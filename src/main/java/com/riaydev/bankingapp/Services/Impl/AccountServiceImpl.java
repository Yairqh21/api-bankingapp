package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
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
    //private final Authentication authentication;

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

        saveTransaction(amount, TransactionType.CASH_DEPOSIT, account, null);
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

        saveTransaction(amount, TransactionType.CASH_WITHDRAWAL, account, null);
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

        saveTransaction(amount, TransactionType.CASH_TRANSFER, sourceAccount, targetAccount);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory() {
        User user = getCurrentAuthenticatedUser();
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        List<Transaction> transactions = transactionRepository.findBySourceAccountAccountNumber(account.getAccountNumber());
    
        return transactions.stream().map(transaction -> new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType().name(),
                transaction.getTransactionDate(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount() != null ? transaction.getTargetAccount().getAccountNumber() : "N/A" 
        )).collect(Collectors.toList());
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
    

    private void verifyPin(User user, String pin) {
        if (!passwordEncoder.matches(pin, user.getPin())) {
            throw new IllegalArgumentException("Invalid PIN");
        }
    }

    private void saveTransaction(BigDecimal amount, TransactionType type, Account sourAccountNumber, Account targetAccountNumber) {

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .transactionDate(new Date())
                .sourceAccount(sourAccountNumber)
                .targetAccount(targetAccountNumber)
                .build();
        transactionRepository.save(transaction);
    }
}
