package com.riaydev.bankingapp.Services.Impl;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.AccountDTO;
import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.AccountService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    @Override
    public AccountDTO getAccountInfo(String email) {
       User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

        Account account = user.getAccounts().get(0);
        return new AccountDTO(account.getAccountNumber(), account.getBalance());
    }

}
