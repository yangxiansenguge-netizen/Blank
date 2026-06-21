package com.blank.app.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CodeGenerator {
    private static final Random RANDOM = new Random();
    private static final String ACTIVATION_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public String generateVerifyCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public String generateVerifyCode() {
        return generateVerifyCode(6);
    }

    public String generateUid() {
        int uid = 1000000 + RANDOM.nextInt(9000000);
        return String.valueOf(uid);
    }

    public String generateOrderNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder(timestamp);
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        if (sb.length() > 32) {
            sb = new StringBuilder(sb.substring(0, 32));
        }
        return sb.toString();
    }

    public String generateActivationCode() {
        StringBuilder sb = new StringBuilder("VIP-");
        for (int i = 0; i < 12; i++) {
            sb.append(ACTIVATION_CHARS.charAt(RANDOM.nextInt(ACTIVATION_CHARS.length())));
            if (i == 3 || i == 7) sb.append('-');
        }
        return sb.toString();
    }
}
