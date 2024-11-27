package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;
import java.util.List;

import com.riaydev.bankingapp.DTO.AccountResponse;
import com.riaydev.bankingapp.DTO.TransactionResponse;

public interface AccountService {

    AccountResponse getAccountInfo(String email);

    void deposit(String pin, BigDecimal amount) throws Exception;

    void withdraw(BigDecimal amount, String pin) throws Exception;

    void transfer(BigDecimal amount, String senderPin, String targetAccountNumber) throws Exception;

    List<TransactionResponse> getTransactionHistory();

}
