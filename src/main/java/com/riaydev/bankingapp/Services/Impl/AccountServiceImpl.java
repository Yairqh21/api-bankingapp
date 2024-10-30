package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.AccountDTO;
import com.riaydev.bankingapp.DTO.TransactionResponse;
import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Transaction;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.TransactionRepository;
import com.riaydev.bankingapp.Repositories.UserRepository;
import com.riaydev.bankingapp.Services.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountDTO getAccountInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

        Account account = user.getAccount().get(0);
        return new AccountDTO(account.getAccountNumber(), account.getBalance());
    }

    @Override
    public void deposit(String pin, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User currentUser = getCurrentAuthenticatedUser();
        verifyPin(currentUser, pin);

        Account account = accountRepository.findByUser(currentUser)
                .orElseThrow(
                        () -> new IllegalArgumentException("Account not found for user: " + currentUser.getEmail()));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        saveTransaction(currentUser, amount, TransactionType.CASH_DEPOSIT, "N/A");
    }

    @Override
    public void withdraw(BigDecimal amount, String pin) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User currentUser = getCurrentAuthenticatedUser();
        verifyPin(currentUser, pin);

        Account account = accountRepository.findByUser(currentUser)
                .orElseThrow(
                        () -> new IllegalArgumentException("Account not found for user: " + currentUser.getEmail()));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        saveTransaction(currentUser, amount, TransactionType.CASH_WITHDRAWAL, "N/A");
    }

    @Override
    public void transfer(BigDecimal amount, String senderPin, String targetAccountNumber) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User userSender = getCurrentAuthenticatedUser();

        Account sourceAccount = accountRepository.findByUser(userSender)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada para el usuario actual"));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        verifyPin(userSender, senderPin);

        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        saveTransaction(userSender, amount, TransactionType.CASH_TRANSFER, targetAccountNumber);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory() {
        User user = getCurrentAuthenticatedUser();
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return transactionRepository.findByAccount(account)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    private User getCurrentAuthenticatedUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void verifyPin(User user, String pin) {
        if (!passwordEncoder.matches(pin, user.getPin())) {
            throw new IllegalArgumentException("Invalid PIN");
        }
    }

    private void saveTransaction(User user, BigDecimal amount, TransactionType type, String targetAccountNumber) {
        Transaction transaction = Transaction.builder()
                .sourceAccountNumber(user.getAccount().get(0).getAccountNumber())
                .amount(amount)
                .transactionType(type)
                .transactionDate(new Date())
                .targetAccountNumber(targetAccountNumber)
                .account(user.getAccount().get(0)) // Asocia la transacción con la cuenta del usuario
                .build();
        transactionRepository.save(transaction);
    }
}
