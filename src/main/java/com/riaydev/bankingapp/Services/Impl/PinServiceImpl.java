package com.riaydev.bankingapp.Services.Impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.PinService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createPin(String pin, String password) {
        User currentUser = getCurrentAuthenticatedUser();

        if (! passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        currentUser.setPin(passwordEncoder.encode(pin));
        userRepository.save(currentUser);
    }

    @Override
    public void updatePin(String oldPin, String password, String newPin) {
        User currentUser = getCurrentAuthenticatedUser();

        if (! passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (! passwordEncoder.matches(oldPin, currentUser.getPin())) {
            throw new IllegalArgumentException("Invalid pin");
        }

        currentUser.setPin(passwordEncoder.encode(newPin));
        userRepository.save(currentUser);
    }

        private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName(); 
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
