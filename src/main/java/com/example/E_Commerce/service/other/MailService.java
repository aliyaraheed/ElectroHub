package com.example.E_Commerce.service.other;

import com.example.E_Commerce.model.UserDtls;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final Cache otpCache;
    private static final int OTP_LENGTH = 4;

    @Autowired
    public MailService(JavaMailSender mailSender, CacheManager cacheManager) {
        this.mailSender = mailSender;
        this.otpCache = cacheManager.getCache("otpCache");
    }

    public void generateAndSendOtp(String email, String sessionId) {
        String otp = generateOtp();
        sendOtpByEmail(email, otp);

        OtpSessionData otpSessionData = new OtpSessionData(otp, email);
        otpCache.put(sessionId, otpSessionData);
    }

    public void verifyOtp(String userEnteredOtp, String sessionId) throws OtpTimeoutException, OtpVerificationException {
        OtpSessionData otpSessionData = otpCache.get(sessionId, OtpSessionData.class);

        if (otpSessionData == null) {
            throw new OtpVerificationException("OTP session not found. Please request a new OTP.");
        }

        if (otpSessionData.isExpired()) {
            otpCache.evict(sessionId);
            throw new OtpTimeoutException("OTP has expired. Please request a new OTP.");
        }

        if (!otpSessionData.getOtp().equals(userEnteredOtp)) {
            throw new OtpVerificationException("Incorrect OTP. Please enter the correct OTP.");
        }

        otpCache.evict(sessionId);
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private void sendOtpByEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("OTP for Verification");
        message.setText("Your OTP for verification is: " + otp);
        mailSender.send(message);
    }

    public static class OtpSessionData {
        private final String otp;
        private final String email;
        private final LocalDateTime expirationTime;

        public OtpSessionData(String otp, String email) {
            this.otp = otp;
            this.email = email;
            this.expirationTime = LocalDateTime.now().plusMinutes(5);
        }

        public String getOtp() {
            return otp;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }

    public static class OtpTimeoutException extends Exception {
        public OtpTimeoutException(String message) {
            super(message);
        }
    }

    public static class OtpVerificationException extends Exception {
        public OtpVerificationException(String message) {
            super(message);
        }
    }
}
