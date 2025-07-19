package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenResponse;
import com.riaydev.bankingapp.DTO.UserRequest;
import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.ResourceNotFoundException;
import com.riaydev.bankingapp.Exceptions.UnauthorizedException;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Security.JwtTokenProvider;
import com.riaydev.bankingapp.Services.UserService;
import com.riaydev.bankingapp.Utils.UserRegistrationRequest;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

        @Autowired
        private UserRepository userRepository;
        @Autowired
        private AccountRepository accountRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtTokenProvider jwtpProvider;
        @Autowired
        private UserRegistrationRequest uRequest;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

                User userEntity = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found for the given identifier: "+email));

                // Retornar el UserDetails con las autoridades correspondientes
                return new org.springframework.security.core.userdetails.User(
                                userEntity.getEmail(), // El nombre de usuario será el email
                                userEntity.getPassword(),
                                //userEntity.isEnabled(), // Activo
                                //userEntity.isAccountNonExpired(), // Cuenta no expirada
                                //userEntity.isCredentialsNonExpired(), // Credenciales no expiradas
                                //userEntity.isAccountNonLocked(), // Cuenta no bloqueada
                                Collections.emptyList()); // Lista de roles y permisos
        }

        @Override
        public UserRequest registerUser(final UserRequest userDTO){
                if (userRepository.existsByEmailAndPhoneNumber(userDTO.email(), userDTO.phoneNumber())) {
                        throw new BadCredentialsException("User already exists with email or phone number");//400
                }

                if(!uRequest.validateEmail(userDTO.email())){
                        throw new BadCredentialsException("Invalid email: " + userDTO.email() );
                }

                User user = userDTOToUser(userDTO);
                User savedUser = userRepository.save(user);
                createAccount(savedUser);

                return userToUserDTO(savedUser);
        }

        @Override
        public TokenResponse loginUser(final AuthRequest authRequest) {
                
                String username = authRequest.identifier();
                String password = authRequest.password();

                Authentication authentication = this.authenticate(username, password);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                final String accessToken = jwtpProvider.generateToken(authentication);
                return new TokenResponse(accessToken);
        }

        @Override
        public UserRequest getUserInfo(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
                return userToUserDTO(user);
        }

        public UserRequest userToUserDTO(User user) {
                String accountNumber = user.getAccount().isEmpty() ? null : user.getAccount().get(0).getAccountNumber();
                return new UserRequest(
                                user.getName(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getAddress(),
                                accountNumber,
                                user.getPassword());
        }

        public User userDTOToUser(UserRequest userDTO) {
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

        private Authentication authenticate(String username, String password) {
                UserDetails userDetail = loadUserByUsername(username);

                if (userDetail == null) {
                        throw new ResourceNotFoundException("User not found");//401
                }

                if (!passwordEncoder.matches(password, userDetail.getPassword())) {
                        throw new UnauthorizedException("Bad credentials");//401
                }

                return new UsernamePasswordAuthenticationToken(username, password);
        }

}
