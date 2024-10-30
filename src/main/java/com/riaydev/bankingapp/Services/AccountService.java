package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;
import java.util.List;

import com.riaydev.bankingapp.DTO.AccountDTO;
import com.riaydev.bankingapp.DTO.TransactionResponse;

public interface AccountService {

    AccountDTO getAccountInfo(String email);

    void deposit(String pin, BigDecimal amount);
    void withdraw(BigDecimal amount, String pin);
    void transfer(BigDecimal amount, String senderPin, String targetAccountNumber);

    List<TransactionResponse> getTransactionHistory();

}
