package com.blank.app.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyBuilder {
    private static final String PREFIX = "blank:";

    public String verifyCode(String email) {
        return PREFIX + "verify:" + email;
    }

    public String verifyCooldown(String email) {
        return PREFIX + "verify_cooldown:" + email;
    }

    public String tokenBlacklist(String token) {
        return PREFIX + "token_blacklist:" + token;
    }

    public String checkin(Integer userId, String date) {
        return PREFIX + "checkin:" + userId + ":" + date;
    }
}
