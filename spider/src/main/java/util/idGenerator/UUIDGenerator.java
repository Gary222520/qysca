package util.idGenerator;

import java.util.UUID;

public final class UUIDGenerator {
    private UUIDGenerator() {
    }

    /**
     * 生成一个长度为32位的UUID字符串
     *
     * @return String uuid
     */
    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        s = s.substring(0, 8) + s.subSequence(9, 13) + s.subSequence(14, 18) + s.substring(19, 23) + s.substring(24);
        s = s.substring(6, 8) + s.substring(4, 6) + s.substring(2, 4) + s.substring(0, 2) + s.substring(10, 12)
                + s.substring(8, 10) + s.substring(14, 16) + s.substring(12, 14) + s.substring(16);
        return s.toUpperCase();
    }

    /**
     * 生成一个长度为16位的UUID字符串
     *
     * @return String uuid
     */
    public static String getUUID16() {
        String s = UUID.randomUUID().toString();
        s = s.substring(0, 8) + s.subSequence(9, 13) + s.subSequence(14, 18) + s.substring(19, 23) + s.substring(24);
        s = s.substring(6, 8) + s.substring(4, 6) + s.substring(2, 4) + s.substring(0, 2) + s.substring(10, 12)
                + s.substring(8, 10) + s.substring(14, 16) + s.substring(12, 14);
        return s.toUpperCase();
    }

    /**
     * 根据特定字符串生成UUID
     *
     * @param str 特定字符串
     * @return UUID uuid
     */
    public static UUID getRandomUUID(String str) {
        if (str == null) return UUID.randomUUID();
        else return UUID.nameUUIDFromBytes(str.getBytes());
    }

    /**
     * 校验UUID是否正确
     *
     * @param uuid uuid
     * @return boolean 是否正确
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        if (uuid.matches("^[0-9a-zA-Z]{32}$")) return true;
        return uuid.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    /**
     * 生成一个Long类型的id
     *
     * @return Long id
     */
    public static Long getLongId() {
        return System.currentTimeMillis() * 1000 + Long.parseLong(getRandom(3));
    }

    /**
     * 生成一个长度为len的随机字符串
     *
     * @param len 字符串长度
     * @return String 生成的字符串
     */
    public static String getRandom(int len) {
        int rs = (int) ((Math.random() * 9 + 1) * Math.pow(10, len - 1));
        return String.valueOf(rs);
    }

    /**
     * 根据前缀生成一个32位的字符串
     *
     * @param prefix 前缀
     * @return String 生成的字符串
     */
    public static String get32StrWithPrefix(String prefix) {
        return get32StrWithPrefixSuffix(prefix, "");
    }

    /**
     * 根据后缀生成一个32位的字符串
     *
     * @param suffix 后缀
     * @return String 生成的字符串
     */
    public static String get32StrWithSuffix(String suffix) {
        return get32StrWithPrefixSuffix("", suffix);
    }

    /**
     * 根据前缀和后缀生成一个32位的字符串
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return String 生成的字符串
     */
    public static String get32StrWithPrefixSuffix(String prefix, String suffix) {
        String uuid = getUUID();
        while (prefix.length() + suffix.length() > uuid.length()) {
            if (prefix.length() > 0) prefix = prefix.substring(1);
            if (suffix.length() > 0) suffix = suffix.substring(1);
        }
        return prefix + uuid.substring(prefix.length(), uuid.length() - suffix.length()) + suffix;
    }
}
