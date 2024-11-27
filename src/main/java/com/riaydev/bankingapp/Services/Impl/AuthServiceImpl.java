package com.riaydev.bankingapp.Services.Impl;

import java.time.Instant;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.Otp;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.ForbiddenException;
import com.riaydev.bankingapp.Repositories.OtpRepository;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.AuthService;
import com.riaydev.bankingapp.Services.EmailService;
import com.riaydev.bankingapp.Utils.OtpUtils;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    
        private final UserRepository userRepository;
        private final OtpRepository otpRepository;
        private final OtpUtils otpUtils;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
    
        @Override
        public void sendOtp(String identifier) {
            String otp = otpUtils.generateOtp();
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Otp newOtp = Otp.builder()
                .user(user)
                .otp(otp)
                .otpExpiration(Instant.now().plusSeconds(300))// Expira en 5 minutos
                .build();
            otpRepository.save(newOtp);
            emailService.sendOtpEmail(user.getEmail(), otp);
        }
        @Override
        public String verifyOtp(String identifier, String otp) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Otp verifyOtp = otpRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Not found otp for this user: " + user.getEmail()));
        
            if (!otp.equals(verifyOtp.getOtp()) || Instant.now().isAfter(verifyOtp.getOtpExpiration())) {
                throw new ForbiddenException("Invalid or expired OTP");
            }
        
            String resetToken = otpUtils.generateResetToken();
            verifyOtp.setResetToken(resetToken);
            otpRepository.save(verifyOtp); 
            return resetToken;
        }
        
        @Override
        public void resetPassword(String identifier, String resetToken, String newPassword) {
            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            Otp userOtp = otpRepository.findByUserId(user.getId())
                    .orElseThrow( () ->new UsernameNotFoundException("Not found otp for this user: "+ user.getEmail()));  

            if (!resetToken.equals(userOtp.getResetToken())) {
                throw new BadCredentialsException("Invalid reset token");
            }
    
            user.setPassword(passwordEncoder.encode(newPassword)); 
            userOtp.setResetToken(null); 
            userRepository.save(user);
            otpRepository.save(userOtp);
        }
    }
    
