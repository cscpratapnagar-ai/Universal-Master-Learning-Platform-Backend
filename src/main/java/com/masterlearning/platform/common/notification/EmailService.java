package com.masterlearning.platform.common.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from:no-reply@masterlearning.local}") String from,
                        @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public void sendWelcome(String email, String firstName) {
        send(email, "Welcome to Master Learning",
                "Hello " + safe(firstName) + ",\n\nYour Master Learning account is ready. You can now sign in and start learning.\n\n" + frontendUrl + "/auth/login");
    }

    public void sendPasswordReset(String email, String firstName, String rawToken) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + java.net.URLEncoder.encode(rawToken, java.nio.charset.StandardCharsets.UTF_8);
        send(email, "Reset your Master Learning password",
                "Hello " + safe(firstName) + ",\n\nWe received a request to reset your password. This link expires in 15 minutes and can only be used once.\n\nReset password:\n" + resetUrl + "\n\nIf you did not request this, you can safely ignore this email.");
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Learner" : value.trim();
    }
}
