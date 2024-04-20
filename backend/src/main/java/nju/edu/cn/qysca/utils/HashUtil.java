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
        List<HashDO> hashes = new ArrayList<>();

        // 创建多个 MessageDigest 实例
        MessageDigest md5Digest;
        MessageDigest sha1Digest;
        MessageDigest sha256Digest;
        MessageDigest sha512Digest;
        MessageDigest sha3_384Digest;
        MessageDigest sha3_256Digest;
        MessageDigest sha3_512Digest;

        try {
            md5Digest = MessageDigest.getInstance("MD5");
            sha1Digest = MessageDigest.getInstance("SHA-1");
            sha256Digest = MessageDigest.getInstance("SHA-256");
            sha512Digest = MessageDigest.getInstance("SHA-512");
            sha3_384Digest = MessageDigest.getInstance("SHA3-384");
            sha3_256Digest = MessageDigest.getInstance("SHA3-256");
            sha3_512Digest = MessageDigest.getInstance("SHA3-512");
        } catch (Exception e) {
            log.error("无法找到指定的哈希算法", e);
            return new ArrayList<>();
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建 HttpGet 请求并设置请求头
            HttpGet httpGet = new HttpGet(jarUrl);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            httpGet.addHeader("Accept", "application/zip,application/octet-stream");

            // 执行 HTTP 请求
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // 确保响应状态为200
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("爬取jar包失败：" + jarUrl);
                    return new ArrayList<>();
                }

                // 获取响应实体并在下载同时计算哈希值
                HttpEntity entity = response.getEntity();
                try (InputStream in = entity.getContent()) {
                    byte[] buffer = new byte[262144]; // 256 KB buffer
                    int bytesRead;

                    // 在下载同时更新多个哈希值
                    while ((bytesRead = in.read(buffer)) != -1) {
                        md5Digest.update(buffer, 0, bytesRead);
                        sha1Digest.update(buffer, 0, bytesRead);
                        sha256Digest.update(buffer, 0, bytesRead);
                        sha512Digest.update(buffer, 0, bytesRead);
                        sha3_384Digest.update(buffer, 0, bytesRead);
                        sha3_256Digest.update(buffer, 0, bytesRead);
                        sha3_512Digest.update(buffer, 0, bytesRead);
                    }

                    // 计算最终的哈希值
                    hashes.add(new HashDO("MD5", bytesToHex(md5Digest.digest())));
                    hashes.add(new HashDO("SHA-1", bytesToHex(sha1Digest.digest())));
                    hashes.add(new HashDO("SHA-256", bytesToHex(sha256Digest.digest())));
                    hashes.add(new HashDO("SHA-512", bytesToHex(sha512Digest.digest())));
                    hashes.add(new HashDO("SHA3-384", bytesToHex(sha3_384Digest.digest())));
                    hashes.add(new HashDO("SHA3-256", bytesToHex(sha3_256Digest.digest())));
                    hashes.add(new HashDO("SHA3-512", bytesToHex(sha3_512Digest.digest())));
                }

                // 确保释放实体资源
                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            log.error("爬取jar包失败：" + jarUrl, e);
            return new ArrayList<>();
        }

        return hashes;
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
