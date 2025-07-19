package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.ForbiddenException;
import com.riaydev.bankingapp.Exceptions.InsufficientFundsException;
import com.riaydev.bankingapp.Exceptions.ResourceNotFoundException;
import com.riaydev.bankingapp.Exceptions.UnauthorizedException;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService {

    // private static final ThreadLocal<User> authenticatedUserCache = new
    // ThreadLocal<>();

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // public User getCurrentAuthenticatedUser() {
    // // Verifica si ya está almacenado en la caché local
    // if (authenticatedUserCache.get() != null) {
    // return authenticatedUserCache.get();
    // }

    // // Si no está en la caché, busca en el repositorio
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // if (authentication == null || !authentication.isAuthenticated()) {
    // throw new IllegalStateException("User authentication error");
    // }

    // String email = authentication.getName();
    // User user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new UsernameNotFoundException("Authentication error"));

    // // Almacena el usuario en la caché local
    // authenticatedUserCache.set(user);
    // return user;
    // }

    // public void clearAuthenticatedUserCache() {
    // // Limpia la caché después de procesar la solicitud
    // authenticatedUserCache.remove();
    // }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User authentication error");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication error"));
    }

    public void verifyPin(String pin) {
        User currentUser = getCurrentAuthenticatedUser();
        if (!passwordEncoder.matches(pin, currentUser.getPin())) {
            throw new ForbiddenException("Invalid PIN.");
        }
    }

    public Account getAccountForCurrentUser() {
        User user = getCurrentAuthenticatedUser();
        return accountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public void validateSufficientBalance(Account account, BigDecimal amount) throws InsufficientFundsException {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }
    }
}
