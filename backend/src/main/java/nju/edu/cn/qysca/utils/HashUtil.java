package nju.edu.cn.qysca.utils;


import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.domain.component.dos.HashDO;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Slf4j
public class HashUtil {

    /**
     * 对指定url的jar包进行hash算法，并封装为List<HashDO>
     *
     * @param jarUrl jar包url
     * @return List<HashDO>
     */
    public static List<HashDO> getHashes(String jarUrl) {
        File file = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(jarUrl);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200){
                log.error("爬取jar包失败：" + jarUrl);
                return new ArrayList<>();
            }
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            file = File.createTempFile("tempJar", ".jar");
            try (InputStream in = entity.getContent();
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

                byte[] buffer = new byte[65536]; // 64 KB buffer
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            // 确保释放实体资源
            EntityUtils.consume(entity);
            httpClient.close();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("爬取jar包失败：" + jarUrl);
            return new ArrayList<>();
        }

        // 调用哈希算法
        List<HashDO> hashes = new ArrayList<>();
        hashes.add(new HashDO("MD5", hash(file, "MD5")));
        hashes.add(new HashDO("SHA-1", hash(file, "SHA-1")));
        hashes.add(new HashDO("SHA-256", hash(file, "SHA-256")));
        hashes.add(new HashDO("SHA-512", hash(file, "SHA-512")));
        hashes.add(new HashDO("SHA-384", hash(file, "SHA-384")));
        hashes.add(new HashDO("SHA3-384", hash(file, "SHA3-384")));
        hashes.add(new HashDO("SHA3-256", hash(file, "SHA-256")));
        hashes.add(new HashDO("SHA3-512", hash(file, "SHA-512")));

        return hashes;
    }

    /**
     * 对指定文件进行hash
     *
     * @param file File 需要hash的文件
     * @param alg  hash算法
     * @return hash值
     */
    private static String hash(File file, String alg) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance(alg);
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("不存在该哈希算法: " + alg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字符数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
