package com.gxt.identity.config;

import com.gxt.identity.port.OtpDeliveryPort;
import com.gxt.identity.port.impl.LoggingOtpDeliveryPort;
import com.gxt.identity.port.impl.SmtpOtpDeliveryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class OtpDeliveryConfig {

    @Bean
    public OtpDeliveryPort otpDeliveryPort(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${gxt.otp.delivery:log}") String delivery,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${gxt.mail.from:connect.gxt@gmail.com}") String fromAddress,
            @Value("${gxt.mail.from-name:gxt-app}") String fromName) {
        boolean smtpRequested = "smtp".equalsIgnoreCase(delivery);
        boolean credentialsPresent = StringUtils.hasText(username) && StringUtils.hasText(password);
        JavaMailSender sender = mailSender.getIfAvailable();
        if (smtpRequested && credentialsPresent && sender != null) {
            return new SmtpOtpDeliveryPort(sender, fromAddress, fromName);
        }
        if (smtpRequested) {
            log.warn("gxt.otp.delivery=smtp but mail credentials are missing; using log delivery");
        }
        return new LoggingOtpDeliveryPort();
    }
}
