package com.riaydev.bankingapp.Utils;

import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Random;

@Component
public class OtpUtils {

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}

