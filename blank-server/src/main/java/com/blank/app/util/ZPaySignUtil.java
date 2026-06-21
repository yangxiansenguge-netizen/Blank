package com.blank.app.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.*;

@Component
public class ZPaySignUtil {

    public String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 error", e);
        }
    }

    public String buildSign(Map<String, Object> params, String key) {
        List<String> sortedKeys = new ArrayList<>(params.keySet());
        Collections.sort(sortedKeys);

        StringBuilder sb = new StringBuilder();
        for (String k : sortedKeys) {
            if ("sign".equals(k) || "sign_type".equals(k)) continue;
            Object v = params.get(k);
            if (v == null || "".equals(String.valueOf(v))) continue;
            sb.append(k).append("=").append(v).append("&");
        }
        sb.append(key);
        return md5(sb.toString());
    }
}
