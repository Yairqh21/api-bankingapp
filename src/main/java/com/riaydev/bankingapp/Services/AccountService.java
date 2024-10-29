package com.riaydev.bankingapp.Services;

import com.riaydev.bankingapp.DTO.AccountDTO;

public interface AccountService {

    AccountDTO getAccountInfo(String email);

}
