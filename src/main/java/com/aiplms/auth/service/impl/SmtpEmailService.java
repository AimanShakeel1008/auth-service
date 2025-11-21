package com.aiplms.auth.service.impl;

import com.aiplms.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            // from will be taken from spring.mail.username or configured default
            mailSender.send(msg);
            log.debug("Sent verification email to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            // in dev we don't want to fail registration if email fails hard — surface gracefully
            // but rethrow if you want registration to fail on email failure
            throw ex;
        }
    }
}
