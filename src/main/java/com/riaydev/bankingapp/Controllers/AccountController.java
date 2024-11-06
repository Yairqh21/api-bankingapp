package com.riaydev.bankingapp.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.CreatePinRequest;
import com.riaydev.bankingapp.DTO.DepositAndWithdrawRequest;
import com.riaydev.bankingapp.DTO.TransactionResponse;
import com.riaydev.bankingapp.DTO.TransferRequest;
import com.riaydev.bankingapp.DTO.UpdatePinRequest;
import com.riaydev.bankingapp.Services.AccountService;
import com.riaydev.bankingapp.Services.PinService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final PinService pinService;
    private final AccountService accountService;

    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@RequestBody CreatePinRequest request) {
        pinService.createPin(request.pin(), request.password());        
        return ResponseEntity.ok(Map.of("msg", "PIN created successfully"));
    }

    @PutMapping("/update-pin")
    public ResponseEntity<?> updatePin(@RequestBody UpdatePinRequest request) {
        pinService.updatePin(request.oldPin(), request.password(), request.newPin());        
        return ResponseEntity.ok(Map.of("msg", "PIN updated successfully"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositAndWithdrawRequest request) {
        accountService.deposit(request.pin(), request.amount());
        return ResponseEntity.ok(Map.of("msg", "Cash deposited successfully"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody DepositAndWithdrawRequest request) {
        accountService.withdraw(request.amount(), request.pin());
        return ResponseEntity.ok(Map.of("msg", "Cash withdrawn successfully"));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        accountService.transfer( request.amount(), request.pin(), request.targetAccountNumber());
        return ResponseEntity.ok(Map.of("msg", "Fund transferred successfully"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory() {
        List<TransactionResponse> history = accountService.getTransactionHistory();
        return ResponseEntity.ok(history);
    }
    

}
