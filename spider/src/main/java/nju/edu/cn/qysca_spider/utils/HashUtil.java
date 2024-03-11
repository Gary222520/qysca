package nju.edu.cn.qysca_spider.utils;

import nju.edu.cn.qysca_spider.domain.HashDO;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
            InputStream in = new URL(jarUrl).openStream();
            //创建临时文件
            file = File.createTempFile("temp", "");
            //写入jar包
            byte[] buffer = new byte[8192]; // 8 KB buffer
            int bytesRead;
            OutputStream outputStream = new FileOutputStream(file);
            while ((bytesRead = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (file == null) {
            System.err.println("无效的jar包url: " + jarUrl);
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
            System.err.println("不存在该哈希算法: " + alg);
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
