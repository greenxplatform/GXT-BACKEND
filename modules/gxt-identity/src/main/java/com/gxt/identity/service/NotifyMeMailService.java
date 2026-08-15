package com.gxt.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class NotifyMeMailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;
    private final boolean smtpReady;

    public NotifyMeMailService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${gxt.mail.from:connect.gxt@gmail.com}") String fromAddress,
            @Value("${gxt.mail.from-name:gxt-app}") String fromName) {
        this.mailSender = mailSender.getIfAvailable();
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.smtpReady = this.mailSender != null
                && StringUtils.hasText(username)
                && StringUtils.hasText(password);
    }

    public void sendInterestRegistered(String name, String toEmail) {
        String subject = "Your GXT interest is registered";
        String body = """
                Hi %s,

                Thanks for signing up to stay tuned. Your interest in GXT has been registered.

                We'll email you when we have product updates. If you're selected for early access, that email will include a private signup and login link.

                No spam. Just updates that matter.

                — %s
                """.formatted(name, fromName);

        if (!smtpReady) {
            log.info("GXT NOTIFY-ME email (log delivery) to {}: {}\n{}", toEmail, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromName + " <" + fromAddress + ">");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("GXT NOTIFY-ME confirmation sent to {}", toEmail);
    }
}
