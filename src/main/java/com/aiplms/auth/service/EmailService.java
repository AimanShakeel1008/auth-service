package com.aiplms.auth.service;

public interface EmailService {
    /**
     * Send simple plain-text email.
     */
    void send(String to, String subject, String body);
}
