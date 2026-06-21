package com.blank.app.service;

public interface EmailService {
    void sendVerificationEmail(String to, String code, int expiryMinutes);
}
