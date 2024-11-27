package com.riaydev.bankingapp.Services.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.ForbiddenException;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.PinService;
import com.riaydev.bankingapp.Services.SecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    @Override
    public void createPin(String pin, String password) {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new ForbiddenException("Invalid password");
        }

        currentUser.setPin(passwordEncoder.encode(pin));
        userRepository.save(currentUser);
    }

    @Override
    public void updatePin(String oldPin, String password, String newPin) {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new ForbiddenException("Password incorrect");
        }

        if (!passwordEncoder.matches(oldPin, currentUser.getPin())) {
            throw new ForbiddenException("Invalid pin");
        }

        currentUser.setPin(passwordEncoder.encode(newPin));
        userRepository.save(currentUser);
    }
}
