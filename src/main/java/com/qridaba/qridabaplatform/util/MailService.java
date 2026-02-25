package com.qridaba.qridabaplatform.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject("Verify your email - QriDaba");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>"
                    +
                    "  <div style='text-align: center; margin-bottom: 20px;'>" +
                    "    <img src='https://avatars.githubusercontent.com/u/245549982?s=96&v=4' alt='Qridaba Logo' style='width: 80px; border-radius: 50%;'>"
                    +
                    "    <h1 style='color: #2d3436; margin-top: 10px;'>Welcome to <span style='color: #e74c3c;'>Qridaba</span>!</h1>"
                    +
                    "  </div>" +
                    "  <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; text-align: center;'>"
                    +
                    "    <p style='font-size: 16px; color: #636e72;'>Please use the following code to verify your email address:</p>"
                    +
                    "    <div style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #e74c3c; margin: 20px 0; padding: 10px; border: 2px dashed #e74c3c; display: inline-block;'>"
                    + code + "</div>" +
                    "    <p style='font-size: 14px; color: #b2bec3;'>This code will expire in 15 minutes for your security.</p>"
                    +
                    "  </div>" +
                    "  <div style='margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; text-align: center; color: #b2bec3; font-size: 12px;'>"
                    +
                    "    <p>&copy; 2025 Qridaba Platform. All rights reserved.</p>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Verification email sent to {} with code: {}", to, code);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}. Verification code: {}", to, e.getMessage(), code);
            // In development, we don't want to block the user if email fails
            log.info("DEVELOPMENT MODE: Please use code {} for verification", code);
        }
    }

    public void sendCredentialsEmail(String to, String password, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject("Your QriDaba Account Credentials");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>" +
                    "  <div style='text-align: center; margin-bottom: 20px;'>" +
                    "    <img src='https://avatars.githubusercontent.com/u/245549982?s=96&v=4' alt='Qridaba Logo' style='width: 80px; border-radius: 50%;'>" +
                    "    <h1 style='color: #2d3436; margin-top: 10px;'>Hello, <span style='color: #e74c3c;'>" + firstName + "</span>!</h1>" +
                    "  </div>" +
                    "  <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; text-align: center;'>" +
                    "    <p style='font-size: 16px; color: #636e72;'>Your account has been created. Here is your temporary password:</p>" +
                    "    <div style='font-size: 24px; font-weight: bold; color: #e74c3c; margin: 20px 0; padding: 10px; border: 2px dashed #e74c3c; display: inline-block;'>" +
                    password + "</div>" +
                    "    <p style='font-size: 14px; color: #b2bec3;'>Please change this password immediately after your first login.</p>" +
                    "  </div>" +
                    "  <div style='margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; text-align: center; color: #b2bec3; font-size: 12px;'>" +
                    "    <p>&copy; 2026 Qridaba Platform. All rights reserved.</p>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Credentials email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send credentials email to {}: {}", to, e.getMessage());
        }
    }
}
