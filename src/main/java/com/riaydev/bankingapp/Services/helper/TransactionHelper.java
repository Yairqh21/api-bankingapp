package com.riaydev.bankingapp.Services.helper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Transaction;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Repositories.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionHelper {

    private final TransactionRepository transactionRepository;

    public void createTransaction(Account account, BigDecimal amount, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .sourceAccount(account)
                .amount(amount)
                .transactionType(type)
                .transactionDate(LocalDateTime.now())
                .targetAccount(null)
                .build();
        transactionRepository.save(transaction);
    }

}
