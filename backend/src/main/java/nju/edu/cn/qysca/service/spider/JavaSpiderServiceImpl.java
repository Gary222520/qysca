package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.dao.component.JavaDependencyTableDao;
import nju.edu.cn.qysca.dao.component.JavaDependencyTreeDao;
import nju.edu.cn.qysca.dao.spider.MavenVisitedUrlsDao;
import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import nju.edu.cn.qysca.domain.spider.dos.MavenVisitedUrlsDO;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import nju.edu.cn.qysca.utils.HashUtil;
import nju.edu.cn.qysca.utils.spider.UrlConnector;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JavaSpiderServiceImpl implements JavaSpiderService {


    @Value("${MAVEN_REPO_BASE_URL}")
    private String MAVEN_REPO_BASE_URL;
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Autowired
    private JavaDependencyTreeDao javaDependencyTreeDao;
    @Autowired
    private JavaDependencyTableDao javaDependencyTableDao;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Autowired
    private MavenService mavenService;
    @Autowired
    private MavenVisitedUrlsDao mavenVisitedUrlsDao;

    /**
     * 通过gav爬取组件
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本
     * @return JavaComponentDO
     */
    @Override
    public JavaComponentDO crawlByGav(String groupId, String artifactId, String version) {
        String url = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        return crawl(url);
    }

    /**
     * 通过gav获取其pom文件内容
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return pomString pom文件内容
     */
    @Override
    public String getPomStrByGav(String groupId, String artifactId, String version) {
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomUrlInDirectory(downloadUrl);
        if (pomUrl == null) {
            return null;
        }
        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;
        return document.outerHtml();
    }

    @Override
    public void crawlDirectory(String directoryUrl) {
        // 如果该url已被访问过，跳过
        if (mavenVisitedUrlsDao.findByUrl(directoryUrl) != null) {
            return;
        }

        Document document = getDocumentByUrl(directoryUrl);
        if (document == null) {
            mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, false, true, false));
            return;
        }

        boolean isLastLevel = true;
        Elements links = document.select("a[href]");
        // 遍历目录下所有链接
        for (Element link : links) {
            String fileAbsUrl = link.absUrl("href");
            // 如果该目录下的链接为目录，说明自身不是最后一层目录，同时递归爬取该链接
            if (fileAbsUrl.endsWith("/") && fileAbsUrl.length() > directoryUrl.length()) {
                isLastLevel = false;
                crawlDirectory(fileAbsUrl);
            }
        }

        if (isLastLevel) {
            // 如果该目录为最后一层，进行爬取
            synchronized (this) {

                String[] parts = directoryUrl.substring(MAVEN_REPO_BASE_URL.length()).split("/");
                String groupId = String.join(".", Arrays.copyOfRange(parts, 0, parts.length-2));
                String artifactId = parts[parts.length - 2];
                String version = parts[parts.length - 1];
                if (javaComponentDao.findByNameAndVersion(groupId+":"+artifactId, version)!=null) {
                    // 数据库已有，跳过
                    mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, true, false));
                    return;
                }
                // 数据库没有，爬取
                JavaComponentDO javaComponentDO = crawl(directoryUrl);
                if (javaComponentDO == null) {
                    // 爬取失败
                    mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, false, true, false));
                    return;
                }
                javaComponentDao.save(javaComponentDO);
                // 记录该url已被访问过
                mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, true, false));
            }
        } else {
            mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, false, false));
        }


    }

    /**
     * 递归地爬取目录url下（带依赖的）所有组件
     *
     * @param directoryUrl 要爬取的目录
     */
    @Override
    public void crawlDirectoryWithDependency(String directoryUrl) {
        // 如果该url已被访问过，跳过
        if (mavenVisitedUrlsDao.findByUrl(directoryUrl) != null) {
            return;
        }

        Document document = getDocumentByUrl(directoryUrl);
        if (document == null) {
            mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, false, true, true));
            return;
        }

        boolean isLastLevel = true;
        Elements links = document.select("a[href]");
        // 遍历目录下所有链接
        for (Element link : links) {
            String fileAbsUrl = link.absUrl("href");
            // 如果该目录下的链接为目录，说明自身不是最后一层目录，同时递归爬取该链接
            if (fileAbsUrl.endsWith("/") && fileAbsUrl.length() > directoryUrl.length()) {
                isLastLevel = false;
                crawlDirectoryWithDependency(fileAbsUrl);
            }
        }
        // 如果该目录为最后一层，进行爬取
        if (isLastLevel) {
            synchronized (this) {
                System.out.println();
                System.out.println("开始爬取：" + directoryUrl);
                //爬取组件
                JavaComponentDO javaComponentDO = crawl(directoryUrl);
                if (javaComponentDO == null) {
                    mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, false, true, true));
                    return;
                }
                if (javaComponentDao.findByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion()) == null)
                    javaComponentDao.save(javaComponentDO);
                if (javaDependencyTreeDao.findByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion()) != null) {
                    // 检查是否已有依赖树，有就跳过
                    mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, true, true));
                    return;
                }

                JavaDependencyTreeDO javaDependencyTreeDO = mavenService.spiderDependency(javaComponentDO.getName().split(":")[0], javaComponentDO.getName().split(":")[1], javaComponentDO.getVersion());
                List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(javaDependencyTreeDO);
                javaDependencyTreeDao.save(javaDependencyTreeDO);
                javaDependencyTableDao.saveAll(javaDependencyTableDOList);

                // 记录该url已被访问过
                mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, true, true));
            }
        } else {
            mavenVisitedUrlsDao.save(new MavenVisitedUrlsDO(null, directoryUrl, true, false, true));
        }


    }

    /**
     * 爬取url下的组件，不爬取其依赖
     * 只会获取该组件自身的组件信息
     *
     * @param url 组件的url，该url目录下应该包含pom文件、（可选）jar包
     * @return JavaComponentDO
     */
    private JavaComponentDO crawl(String url) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStamp = dateFormat.format(date);
        System.out.println(timeStamp + " crawling :" + url);
        // 爬取pom文件中的组件信息
        String pomUrl = findPomUrlInDirectory(url);
        if (pomUrl == null) {
            return null;
        }

        Document document = getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        JavaComponentDO javaComponentDO = convertToComponentDO(document.outerHtml(), pomUrl);
        if (javaComponentDO == null)
            return null;

        JavaComponentDO searchResult = javaComponentDao.findByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion());
        if (searchResult != null) {
            // 表示该组件的组件信息已被爬取过
            return searchResult;
        }

        // 爬取jar包，生成hash信息
        String jarUrl = findJarUrlInDirectory(url);
        if (jarUrl != null) {
            javaComponentDO.setHashes(HashUtil.getHashes(jarUrl));
        }

        return javaComponentDO;
    }

    /**
     * 给定一个url地址，爬取其内容至一个document
     *
     * @param url url地址
     * @return Document org.jsoup.nodes.Document
     */
    private Document getDocumentByUrl(String url) {
        try {
            // 每次爬取url时休眠一定时间，防止被ban
            //Thread.sleep(sleepTime);
            // 连接到url并获取其内容
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            System.err.println("can't visit or it is invalid: " + url);
            return null;
        }
    }

    /**
     * 在指定目录url下寻找pom文件，并返回其url
     *
     * @param directoryUrl 目录url
     * @return pomUrl
     */
    private String findPomUrlInDirectory(String directoryUrl) {
        Document document = getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 从目录中提取出工件号和版本号
        String[] parts = directoryUrl.split("/");
        String artifactId = parts[parts.length - 2];
        String version = parts[parts.length - 1];

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.pom结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般只有形如“artifactId-version.pom"的才是需要的pom文件
            if (fileAbsUrl.endsWith(artifactId + "-" + version + ".pom")) {
                return fileAbsUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
    }

    /**
     * 在指定目录下寻找jar包，并返回其url
     *
     * @param directoryUrl 目录url
     * @return jarUrl
     */
    private String findJarUrlInDirectory(String directoryUrl) {
        Document document = getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 从目录中提取出工件号和版本号
        String[] parts = directoryUrl.split("/");
        String artifactId = parts[parts.length - 2];
        String version = parts[parts.length - 1];

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.jar结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般只有形如“artifactId-version.jar"的才是需要的jar包
            if (fileAbsUrl.endsWith(artifactId + "-" + version + ".jar")) {
                return fileAbsUrl;
            }
        }
        return null;
    }

    /**
     * 将爬取的pom字符串转换为JavaComponentDO
     *
     * @param pomString
     * @param pomUrl    pom文件url
     * @return JavaComponentDO
     */
    private JavaComponentDO convertToComponentDO(String pomString, String pomUrl) {
        // 从pom url中提取groupId, artifactId, and version
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, MAVEN_REPO_BASE_URL.split("/").length, parts.length - 3));

        // 将jsoup document转化为maven-model
        Model model = convertToModel(pomString);
        if (model == null) {
            return null;
        }

        JavaComponentDO javaComponentDO = new JavaComponentDO();
        javaComponentDO.setName(groupId + ":" + artifactId);
        javaComponentDO.setVersion(version);

        javaComponentDO.setJName(model.getName() == null ? "-" : model.getName());
        javaComponentDO.setLanguage("java");
        javaComponentDO.setType("opensource");
        javaComponentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());

        javaComponentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        javaComponentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        javaComponentDO.setSourceUrl(model.getScm() == null ? "-" : (model.getScm().getUrl() == null ? "-" : model.getScm().getUrl()));

        javaComponentDO.setPUrl(getMavenPUrl(groupId, artifactId, version, model.getPackaging()));
        javaComponentDO.setDevelopers(getDevelopers(model));
        List<String> licenses = new ArrayList<>();
        List<String> license = getLicense(model);
        for (String licenseName : license) {
            licenses.addAll(licenseService.searchLicense(licenseName));
        }
        javaComponentDO.setVulnerabilities(vulnerabilityService.findVulnerabilities(groupId + ":" + artifactId, version, "java").toArray(new String[0]));
        javaComponentDO.setLicenses(licenses.toArray(new String[0]));
        javaComponentDO.setCreator(null);
        javaComponentDO.setState("SUCCESS");
        return javaComponentDO;
    }

    /**
     * 从pomUrl中得到下载地址，即去掉pomUrl中最后一段字符串
     * 例如：https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/即为下载地址
     *
     * @param pomUrl pom url
     * @return download url
     */
    private String getDownloadUrl(String pomUrl) {
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    /**
     * 生成PUrl（仅对maven组件）
     * 例如：pkg:maven/commons-codec/commons-codec@1.15?type=jar
     *
     * @param groupId    组织Id
     * @param artifactId 工件id
     * @param version    版本号
     * @param packaging  打包方式，如pom、jar
     * @return PUrl
     */
    private String getMavenPUrl(String groupId, String artifactId, String version, String packaging) {
        String pUrl = "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
        if (packaging.equals("jar"))
            pUrl += "?type=jar";
        return pUrl;
    }

    /**
     * 从maven-model中获得developerDO列表
     *
     * @param model maven-model
     * @return developerDO列表
     */
    private List<DeveloperDO> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    DeveloperDO developer = new DeveloperDO();
                    developer.setId(mavenDeveloper.getId() == null ? "-" : mavenDeveloper.getId());
                    developer.setName(mavenDeveloper.getName() == null ? "-" : mavenDeveloper.getName());
                    developer.setEmail(mavenDeveloper.getEmail() == null ? "-" : mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从maven-model中获得license列表
     *
     * @param model maven-model
     * @return List<String> license列表
     */
    private List<String> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        List<String> result = new ArrayList<>();
        for (org.apache.maven.model.License mavenLicense : mavenLicenses) {
            result.add(mavenLicense.getName());
        }
        return result;
    }

    /**
     * 将pom string转化为maven-model
     *
     * @param pomString pom.xml转成字符串
     * @return maven-model
     */
    private Model convertToModel(String pomString) {
        Model model = null;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new StringReader(pomString));
            return model;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }
}
