package com.blank.app.service.impl;

import com.blank.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Override
    public void sendVerificationEmail(String to, String code, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Blank - 邮箱验证码");

            StringBuilder sb = new StringBuilder();
            sb.append("<div style='max-width:600px;margin:0 auto;padding:30px;font-family:Arial,sans-serif'>");
            sb.append("<h2 style='color:#333'>您的验证码</h2>");
            sb.append("<p style='color:#666;font-size:14px'>验证码 ").append(expiryMinutes).append(" 分钟内有效：</p>");
            sb.append("<div style='margin:20px 0'>");
            for (char c : code.toCharArray()) {
                sb.append("<span style='display:inline-block;width:40px;height:40px;line-height:40px;text-align:center;background:#f0f0f0;border-radius:8px;margin:0 4px;font-size:20px;font-weight:bold;color:#333'>").append(c).append("</span>");
            }
            sb.append("</div>");
            sb.append("<p style='color:#999;font-size:12px'>如果这不是您本人的操作，请忽略此邮件。</p>");
            sb.append("</div>");

            helper.setText(sb.toString(), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
