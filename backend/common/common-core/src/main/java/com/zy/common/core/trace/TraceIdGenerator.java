package com.zy.common.core.trace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates compact trace IDs: yyyyMMddHHmmssSSS + 8 random [a-zA-Z0-9] chars.
 */
public final class TraceIdGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final char[] RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final int RANDOM_LENGTH = 8;

    private TraceIdGenerator() {
    }

    public static String generate() {
        StringBuilder builder = new StringBuilder(25);
        builder.append(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            builder.append(RANDOM_CHARS[random.nextInt(RANDOM_CHARS.length)]);
        }
        return builder.toString();
    }
}
