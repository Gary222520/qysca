package utils;

import domain.component.HashDO;
import org.jsoup.nodes.Document;
import spider.UrlConnector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HashUtil {

    /**
     * 对指定字符串进行hash算法，并封装为List<HashDO>
     * @param bytes
     * @return List<HashDO>
     */
    public static List<HashDO> getHashes(byte[] bytes){
        List<HashDO> hashes = new ArrayList<>();

        hashes.add(new HashDO("MD5", md5(bytes)));
        hashes.add(new HashDO("SHA-1", sha(bytes, "SHA-1")));
        hashes.add(new HashDO("SHA-256", sha(bytes, "SHA-256")));
        hashes.add(new HashDO("SHA-512", sha(bytes, "SHA-512")));
        hashes.add(new HashDO("SHA-384", sha(bytes, "SHA-384")));
        hashes.add(new HashDO("SHA3-384", sha(bytes, "SHA3-384")));
        hashes.add(new HashDO("SHA3-256", sha(bytes, "SHA-256")));
        hashes.add(new HashDO("SHA3-512", sha(bytes, "SHA-512")));

        return hashes;
    }

    /**
     * MD5哈希算法
     * @param bytes
     * @return hash字符串
     */
    private static String md5(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(bytes);
            return bytesToHex(md5Hash);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SHA哈希算法
     * @param bytes
     * @param alg 算法名
     * @return hash字符串
     */
    private static String sha(byte[] bytes, String alg){
        try {
            MessageDigest md = MessageDigest.getInstance(alg);
            md.update(getSalt());
            byte[] hash = md.digest(bytes);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字节数组转换为十六进制字符串
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

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public static void main(String[] args) {


        String jarUrl = "https://repo.maven.apache.org/maven2/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2-sources.jar";

        try {
            URL url = new URL(jarUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }


            List<HashDO> hashes = HashUtil.getHashes(outputStream.toByteArray());
            System.out.println(hashes);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
