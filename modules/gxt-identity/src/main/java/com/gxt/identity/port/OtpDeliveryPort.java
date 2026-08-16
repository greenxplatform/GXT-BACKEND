package com.gxt.identity.port;

public interface OtpDeliveryPort {
    void sendEmailOtp(String toEmail, String code);
}
