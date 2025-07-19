package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.AccountResponse;
import com.riaydev.bankingapp.DTO.TransactionResponse;
import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Transaction;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.InsufficientFundsException;
import com.riaydev.bankingapp.Exceptions.ResourceNotFoundException;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.TransactionRepository;
import com.riaydev.bankingapp.Services.AccountService;
import com.riaydev.bankingapp.Services.SecurityService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SecurityService securityService;

    @Override
    public AccountResponse getAccountInfo(String email) {
        User user = securityService.getCurrentAuthenticatedUser();

        if (user.getAccount().isEmpty()) {
            throw new ResourceNotFoundException("User has no accounts.");
        }
        Account account = user.getAccount().get(0);
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    @Override
    @Transactional
    public void deposit(String pin, BigDecimal amount) throws Exception {

        securityService.verifyPin(pin);

        Account account = securityService.getAccountForCurrentUser();

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        saveTransaction(amount, TransactionType.CASH_DEPOSIT, account, null);
    }

    @Override
    @Transactional
    public void withdraw(BigDecimal amount, String pin) throws Exception {

        securityService.verifyPin(pin);
        Account account = securityService.getAccountForCurrentUser();

        securityService.validateSufficientBalance(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        saveTransaction(amount, TransactionType.CASH_WITHDRAWAL, account, null);
    }

    @Override
    @Transactional
    public void transfer(BigDecimal amount, String senderPin, String targetAccountNumber)
            throws InsufficientFundsException {

        securityService.verifyPin(senderPin);

        Account sourceAccount = securityService.getAccountForCurrentUser();

        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));

        if (!sourceAccount.getId().equals(targetAccount.getId())) {

            int updatedRows = accountRepository.debitAccount(sourceAccount.getId(), amount);
            if (updatedRows == 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }

            accountRepository.creditAccount(targetAccount.getId(), amount);

            saveTransaction(amount, TransactionType.CASH_TRANSFER, sourceAccount, targetAccount);
        }

        else {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

    }

    @Override
    public List<TransactionResponse> getTransactionHistory() {
        Account account = securityService.getAccountForCurrentUser();

        List<Transaction> transactions = transactionRepository
                .findBySourceAccountAccountNumber(account.getAccountNumber());

        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getTransactionType().name(),
                        transaction.getTransactionDate(),
                        transaction.getSourceAccount().getAccountNumber(),
                        transaction.getTargetAccount() != null ? transaction.getTargetAccount().getAccountNumber()
                                : "N/A"))
                .collect(Collectors.toList());
    }

    private void saveTransaction(BigDecimal amount, TransactionType type, Account sourAccountNumber,
            Account targetAccountNumber) {

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .transactionDate(LocalDateTime.now())
                .sourceAccount(sourAccountNumber)
                .targetAccount(targetAccountNumber)
                .build();
        transactionRepository.save(transaction);
    }
}
