package com.gxt.identity.port.impl;

import com.gxt.identity.port.OtpDeliveryPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingOtpDeliveryPort implements OtpDeliveryPort {

    @Override
    public void sendEmailOtp(String toEmail, String code) {
        log.info("GXT EMAIL OTP for {}: {}", toEmail, code);
    }
}
