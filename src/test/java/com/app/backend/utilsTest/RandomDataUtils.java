package com.app.backend.utilsTest;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomDataUtils {

    public static final String EXAMPLE_COM = "@example.com";
    private static final Random random = new Random();

    public static String randomEmail() {
        return RandomStringUtils.randomAlphabetic(10) + EXAMPLE_COM;
    }

    public static String randomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String randomPassword() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    public static String randomFirstName() {
        return RandomStringUtils.randomAlphabetic(5, 10);
    }

    public static String randomLastName() {
        return RandomStringUtils.randomAlphabetic(5, 10);
    }

    public static int randomInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}
