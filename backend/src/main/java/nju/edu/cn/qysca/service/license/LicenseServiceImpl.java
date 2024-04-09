package nju.edu.cn.qysca.service.license;

import nju.edu.cn.qysca.dao.application.AppComponentDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.license.LicenseDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dos.LicenseTermDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseBriefDTO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoDTO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoTermDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

@Service
public class LicenseServiceImpl implements LicenseService{

    @Autowired
    private LicenseDao licenseDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private AppComponentDao appComponentDao;

    @Autowired
    private AppDependencyTableDao appDependencyTableDao;

    /**
     * 查询许可证
     * @param queryName 待查询许可证名称
     * @return List<String> 匹配到的许可证名称，当精确匹配到时，只会返回一个许可证
     */
    @Override
    public List<String> searchLicense(String queryName){

        List<String> results = new ArrayList<>();
        // 先精确搜素
        LicenseDO licenseDO = exactSearch(queryName);
        if (licenseDO != null){
            results.add(licenseDO.getName());
            return results;
        }

        // 搜索不到时进行模糊搜索
        List<LicenseDO> licenseDOList = fuzzySearch(queryName, 30);
        results = licenseDOList.stream()
                .map(LicenseDO::getName)
                .collect(Collectors.toList());
        return results;
    }

    /**
     * 获取许可证冲突信息
     *
     * @param licenses 许可证列表
     * @return 许可证冲突信息
     */
    @Override
    public LicenseConflictInfoDTO getLicenseConflictInformation(List<LicenseDO> licenses) {

        Set<String> obligationsSet = new HashSet<>();
        Set<String> rightsSet = new HashSet<>();

        // 先记录一共有那些权利和义务条目
        for (LicenseDO license : licenses) {
            license.getObligationsRequired().stream().map(LicenseTermDO::getTitle).forEach(obligationsSet::add);
            license.getObligationsNotRequired().stream().map(LicenseTermDO::getTitle).forEach(obligationsSet::add);
            license.getRightsAllowed().stream().map(LicenseTermDO::getTitle).forEach(rightsSet::add);
            license.getRightsProhibited().stream().map(LicenseTermDO::getTitle).forEach(rightsSet::add);
        }

        List<LicenseConflictInfoTermDTO> obligations_terms = new ArrayList<>();
        List<LicenseConflictInfoTermDTO> rights_terms = new ArrayList<>();

        // 记录每种义务有哪些许可证必须，哪些许可证无需
        for (String obligation : obligationsSet){
            LicenseConflictInfoTermDTO conflictInfoTerm = new LicenseConflictInfoTermDTO();
            conflictInfoTerm.setPos_licenses(new ArrayList<>());
            conflictInfoTerm.setNeg_licenses(new ArrayList<>());

            for (LicenseDO license : licenses) {
                if (containTitle(license.getObligationsRequired(), obligation))
                    conflictInfoTerm.getPos_licenses().add(license);
                if (containTitle(license.getObligationsNotRequired(), obligation))
                    conflictInfoTerm.getNeg_licenses().add(license);
            }
            obligations_terms.add(conflictInfoTerm);
        }

        // 记录每种权利有哪些许可证允许，哪些许可证禁止
        for (String right : rightsSet){
            LicenseConflictInfoTermDTO conflictInfoTerm = new LicenseConflictInfoTermDTO();
            conflictInfoTerm.setPos_licenses(new ArrayList<>());
            conflictInfoTerm.setNeg_licenses(new ArrayList<>());

            for (LicenseDO license : licenses) {
                if (containTitle(license.getRightsAllowed(), right))
                    conflictInfoTerm.getPos_licenses().add(license);
                if (containTitle(license.getRightsProhibited(), right))
                    conflictInfoTerm.getNeg_licenses().add(license);
            }
            rights_terms.add(conflictInfoTerm);
        }

        return new LicenseConflictInfoDTO(obligations_terms, rights_terms);
    }

    /**
     * 判断terms是否有该title
     *
     * @param terms List<LicenseTermDO>
     * @param title Title
     * @return boolean
     */
    private boolean containTitle(List<LicenseTermDO> terms, String title) {
        if (terms != null) {
            for (LicenseTermDO term : terms) {
                if (term.getTitle().equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 精确搜索许可证（忽略大小写）
     * @param name 许可证名称/全名
     * @return LicenseDO 找不到时为null
     */
    private LicenseDO exactSearch(String name) {
        LicenseDO licenseDO = licenseDao.findByNameIgnoreCase(name);
        if (licenseDO != null)
            return licenseDO;
        licenseDO = licenseDao.findByFullNameIgnoreCase(name);
        if (licenseDO != null)
            return licenseDO;
        return null;
    }


    /**
     * 使用编辑距离进行模糊查询
     * @param queryName 许可证名称/全名
     * @param maxThreshold 最大阈值
     * @return List<LicenseDO>
     */
    public List<LicenseDO> fuzzySearch(String queryName, int maxThreshold) {
        List<LicenseDO> licenses = licenseDao.findAll();
        List<LicenseDO> results = new ArrayList<>();

        // 计算name和fullName与查询字符串的编辑距离
        EditDistanceWithWeight editDistanceWithWeight = new EditDistanceWithWeight();
        Map<String, Integer> nameDistance = new HashMap<>();
        Map<String, Integer> fullNameDistance = new HashMap<>();
        for (LicenseDO license: licenses){
            nameDistance.put(license.getName(), editDistanceWithWeight.apply(queryName.toLowerCase(), license.getName().toLowerCase()));
            fullNameDistance.put(license.getFullName(), editDistanceWithWeight.apply(queryName.toLowerCase(), license.getFullName().toLowerCase()));
        }

//        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(nameDistance.entrySet());
//        // 使用 Collections.sort() 方法对 List 进行排序
//        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
//        // 打印排序后的键值对
//        for (Map.Entry<String, Integer> entry : entryList) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//
//        System.out.println();
//        System.out.println();
//        List<Map.Entry<String, Integer>> entryList2 = new ArrayList<>(fullNameDistance.entrySet());
//        // 使用 Collections.sort() 方法对 List 进行排序
//        Collections.sort(entryList2, Comparator.comparing(Map.Entry::getValue));
//        // 打印排序后的键值对
//        for (Map.Entry<String, Integer> entry2 : entryList2) {
//            System.out.println(entry2.getKey() + ": " + entry2.getValue());
//        }

         // 先严格，再逐渐增加阈值使得条件宽松，直到找到合适结果
        int threshold = 0; // 初始阈值为 0
        while (threshold <= maxThreshold) {
            for (LicenseDO license : licenses) {
                // 如果编辑距离小于等于阈值，添加到结果集中
                if (nameDistance.get(license.getName()) <= threshold || fullNameDistance.get(license.getFullName()) <= threshold) {
                    results.add(license);
                }
            }
            // 如果找到了结果，直接返回
            if (!results.isEmpty()) {
                return results;
            }
            // 如果未找到结果，增加阈值，继续搜索
            threshold = threshold + 5;
        }
        return results;
    }

    /**
     * 带权编辑距离
     */
    private static class EditDistanceWithWeight {

        // 定义权重，增加操作的代价较低
        private static final int INSERT_COST = 2;
        private static final int DELETE_COST = 5;
        private static final int REPLACE_COST = 6;
        private static final double MATCH_REWARD = 0.6;

        public int apply(String str1, String str2) {
            int[][] dp = new int[str1.length() + 1][str2.length() + 1];

            // 初始化第一行和第一列
            for (int i = 0; i <= str1.length(); i++) {
                dp[i][0] = i * DELETE_COST;
            }
            for (int j = 0; j <= str2.length(); j++) {
                dp[0][j] = j * INSERT_COST;
            }

            // 计算编辑距离
            for (int i = 1; i <= str1.length(); i++) {
                for (int j = 1; j <= str2.length(); j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        int deleteCost = dp[i - 1][j] + DELETE_COST;
                        int insertCost = dp[i][j - 1] + INSERT_COST;
                        int replaceCost = dp[i - 1][j - 1] + REPLACE_COST;
                        dp[i][j] = Math.min(Math.min(deleteCost, insertCost), replaceCost);
                    }
                }
            }

            int result = dp[str1.length()][str2.length()];

            // 将字符串按空格和横杠分隔成单词数组
            // 每有一个相同的单词，给予一定距离奖励
            String[] words1 = str1.split("[\\s\\-]+");
            String[] words2 = str2.split("[\\s\\-]+");
            // 遍历第一个字符串的单词数组，在第二个字符串中查找相同的单词
            for (String word : words1) {
                if (word.equalsIgnoreCase("license"))
                    continue;
                for (String otherWord : words2) {
                    if (word.equalsIgnoreCase(otherWord)) {
                        result = (int) (result * MATCH_REWARD);
                        break; // 找到一个相同的单词就停止搜索
                    }
                }
            }

            return result;
        }
    }

    @Override
    public Page<LicenseBriefDTO> getLicenseList(String name, String version, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        Page<LicenseDO> temp = licenseDao.getLicenseList(Arrays.asList(applicationDO.getLicenses()), pageable);
        Page<LicenseBriefDTO> result = temp.map(licenseDO -> new LicenseBriefDTO(licenseDO.getName(), licenseDO.getFullName(), licenseDO.getIsOsiApproved(), licenseDO.getIsFsfApproved(), licenseDO.getIsSpdxApproved(), licenseDO.getRiskLevel(), licenseDO.getGplCompatibility()));
        return result;
    }

    @Override
    @Transactional
    public Boolean addAppLicense(String name, String version, String licenseName) {
        // 已经发布或锁定的禁止更新
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        if(applicationDO.getRelease() || applicationDO.getLock()){
            throw new PlatformException(500, "组件已经发布或锁定，禁止更新");
        }
        List<String> licenses = searchLicense(licenseName);
        Set<String> licenseSet = new HashSet<>(Arrays.asList(applicationDO.getLicenses()));
        licenseSet.addAll(licenses);
        applicationDO.setLicenses(licenseSet.toArray(new String[0]));
        applicationDao.save(applicationDO);
        AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(name, version);
        if(appComponentDO != null) {
            appComponentDO.setLicenses(licenseSet.toArray(new String[0]));
            appComponentDao.save(appComponentDO);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteAppLicense(String name, String version, String licenseName) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        if(applicationDO.getRelease() || applicationDO.getLock()){
            throw new PlatformException(500, "组件已经发布或锁定，禁止更新");
        }
        List<String> temp = new ArrayList<>(Arrays.asList(applicationDO.getLicenses()));
        temp.remove(licenseName);
        applicationDO.setLicenses(temp.toArray(new String[0]));
        applicationDao.save(applicationDO);
        AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(name, version);
        if(appComponentDO != null) {
            appComponentDO.setLicenses(temp.toArray(new String[0]));
            appComponentDao.save(appComponentDO);
        }
        return true;
    }

    @Override
    public LicenseDO getLicenseInfo(String name) {
        return licenseDao.findByName(name);
    }

    @Override
    public Page<LicenseBriefDTO> getLicensePage(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<LicenseDO> temp = licenseDao.getLicensePage(name, pageable);
        Page<LicenseBriefDTO> result = temp.map(licenseDO -> new LicenseBriefDTO(licenseDO.getName(), licenseDO.getFullName(), licenseDO.getIsOsiApproved(), licenseDO.getIsFsfApproved(), licenseDO.getIsSpdxApproved(), licenseDO.getRiskLevel(), licenseDO.getGplCompatibility()));
        return result;
    }
}
