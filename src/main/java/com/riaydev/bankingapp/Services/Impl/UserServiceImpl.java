package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenDTO;
import com.riaydev.bankingapp.DTO.UserDTO;
import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Token;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.TokenRepository;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Security.JwtTokenProvider;
import com.riaydev.bankingapp.Services.UserService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtService;

    @Override
    public UserDTO registerUser(final UserDTO userDTO) {
        if (checkIfEmailOrPhoneExists(userDTO.email(), userDTO.phoneNumber())) {
            throw new RuntimeException("User already exists with email or phone number");
        }

        User user = userDTOToUser(userDTO);
        User savedUser = userRepository.save(user);
        createAccount(savedUser);

        return userToUserDTO(savedUser);
    }

    @Override
    public TokenDTO loginUser(final AuthRequest authRequest) {
        final User user = userRepository.findByEmail(authRequest.identifier())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found for the given identifier: " + authRequest.identifier()));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.identifier(),
                            authRequest.password()));
        } catch (BadCredentialsException e) {
            throw e; 
        }

        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return new TokenDTO(accessToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        final Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Override
    public UserDTO getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        return userToUserDTO(user);
    }

    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setIsExpired(true);
                token.setIsRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public UserDTO userToUserDTO(User user) {
        String accountNumber = user.getAccount().isEmpty() ? null : user.getAccount().get(0).getAccountNumber();
        return new UserDTO(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                accountNumber,
                user.getPassword());
    }

    public User userDTOToUser(UserDTO userDTO) {
        return User.builder()
                .name(userDTO.name())
                .email(userDTO.email())
                .phoneNumber(userDTO.phoneNumber())
                .address(userDTO.address())
                .password(passwordEncoder.encode(userDTO.password()))
                .create_at(LocalDateTime.now())
                .build();
    }

    public boolean checkIfEmailOrPhoneExists(String email, String phoneNumber) {
        return userRepository.existsByEmail(email) || userRepository.existsByPhoneNumber(phoneNumber);
    }

    private String createAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .accountNumber(UUID.randomUUID().toString().substring(0, 6))
                .balance(BigDecimal.ZERO)
                .build();

        user.getAccount().add(account);

        accountRepository.save(account);

        return account.getAccountNumber();
    }

    @Override
    public String logout(String token) {
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    
        storedToken.setIsExpired(true);
        storedToken.setIsRevoked(true);
        tokenRepository.save(storedToken);
    
        SecurityContextHolder.clearContext();
    
        return "Logout successful";
    }
    

}
