package com.gxt.identity.port.impl;

import com.gxt.identity.port.OtpDeliveryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
public class SmtpOtpDeliveryPort implements OtpDeliveryPort {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public SmtpOtpDeliveryPort(JavaMailSender mailSender, String fromAddress, String fromName) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void sendEmailOtp(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromName + " <" + fromAddress + ">");
        message.setTo(toEmail);
        message.setSubject("Your GXT verification code");
        message.setText(
                "Your GXT verification code is: " + code + "\n\nIt is valid for 10 minutes.\n\n— gxt-app");
        mailSender.send(message);
        log.info("GXT EMAIL OTP sent to {}", toEmail);
    }
}
