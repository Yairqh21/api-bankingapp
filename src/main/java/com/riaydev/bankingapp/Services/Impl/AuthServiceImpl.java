package com.riaydev.bankingapp.Services.Impl;

import java.time.Instant;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.AuthService;
import com.riaydev.bankingapp.Services.EmailService;
import com.riaydev.bankingapp.Utils.OtpUtils;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    
        private final UserRepository userRepository;
        private final OtpUtils otpUtils;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
    
        @Override
        public void sendOtp(String identifier) {
            String otp = otpUtils.generateOtp();
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            user.setOtp(otp);
            user.setOtpExpiration(Instant.now().plusSeconds(300)); // Expira en 5 minutos
            userRepository.save(user);
            emailService.sendOtpEmail(user.getEmail(), otp);
        }
        @Override
        public String verifyOtp(String identifier, String otp) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            if (!otp.equals(user.getOtp()) || Instant.now().isAfter(user.getOtpExpiration())) {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
    
            String resetToken = otpUtils.generateResetToken();
            user.setResetToken(resetToken);
            userRepository.save(user);
            return resetToken;
        }
        @Override
        public void resetPassword(String identifier, String resetToken, String newPassword) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
            if (!resetToken.equals(user.getResetToken())) {
                throw new IllegalArgumentException("Invalid reset token");
            }
    
            user.setPassword(passwordEncoder.encode(newPassword)); 
            user.setResetToken(null); 
            userRepository.save(user);
        }
    }
    
