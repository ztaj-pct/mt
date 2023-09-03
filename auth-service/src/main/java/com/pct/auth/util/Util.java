package com.pct.auth.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
    public static String generatePassword(int len) {
        String capitalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String smallChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "@#$";

        String values = capitalChars + smallChars + numbers + symbols;

        // Using random method
        Random rndm_method = new Random();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            buffer.append(values.charAt(rndm_method.nextInt(values.length())));

        }
        return buffer.toString();
    }

    public static String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(125521, 987654));
    }

    public static String generateRandomToken() {
        return UUID.randomUUID().toString();
    }

}