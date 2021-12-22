package cn.xxx.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static MessageDigest md;
    private static Random rand = new Random();

    public static int GetRandomNumber(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    public static Boolean GetRandomBoolean() {
        return rand.nextInt(100) > 50;
    }

    public static String GetRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = rand.nextInt(str.length() - 1);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static int[] getRandomIndex(int size, int max) {
        if (size > max) size = max;
        return ThreadLocalRandom.current().ints(0, max).distinct().limit(size).toArray();
    }

    public static String confusionChars(String[] _chars) {
        StringBuilder result = new StringBuilder();
        int confusionCount = Utils.GetRandomNumber(1, _chars.length);
        int[] confustionCharIndexs = Utils.getRandomIndex(confusionCount, _chars.length);
        for (int i = 0; i < _chars.length; i++) {
            int finalI = i;
            if (Arrays.stream(confustionCharIndexs).anyMatch(c -> c == finalI)) {
                result.append(confusionChar(_chars[i]));
            } else {
                result.append(_chars[i]);
            }
        }
        return result.toString();
    }

    public static String confusionChar(String _char) {
        int garbageCount = Utils.GetRandomNumber(2, 5);
        StringBuilder garbage = new StringBuilder();
        for (int i = 0; i < garbageCount; i++) {
            int garbageLength = Utils.GetRandomNumber(3, 6);
            String garbageWord = Utils.GetRandomString(garbageLength);
            garbage.append(garbageWord).append(":");
        }
        return String.format("${%s-%s}", garbage, _char);
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    public static String getCurrentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static byte[] MD5(byte[] src) {
        if (md == null) {
            try {
                md = MessageDigest.getInstance("md5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 not found!");
            }
        }
        byte[] secretBytes = null;
        secretBytes = md.digest(src);
        return secretBytes;
    }
}
