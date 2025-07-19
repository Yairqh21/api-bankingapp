package com.riaydev.bankingapp.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.riaydev.bankingapp.DTO.*;
import com.riaydev.bankingapp.Services.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final PinService pinService;
    private final AccountService accountService;

    @Autowired
    private MarketService marketService;

    @PostMapping("/create-pin")
    public ResponseEntity<?> createPin(@Valid @RequestBody CreatePinRequest request) {
        pinService.createPin(request.pin(), request.password());
        return ResponseEntity.ok(Map.of("msg", "PIN created successfully"));
    }

    @PutMapping("/update-pin")
    public ResponseEntity<?> updatePin(@Valid @RequestBody UpdatePinRequest request) {
        pinService.updatePin(request.oldPin(), request.password(), request.newPin());
        return ResponseEntity.ok(Map.of("msg", "PIN updated successfully"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositAndWithdrawRequest request) throws Exception {
        accountService.deposit(request.pin(), request.amount());
        return ResponseEntity.ok(Map.of("msg", "Cash deposited successfully"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody DepositAndWithdrawRequest request) throws Exception {
        accountService.withdraw(request.amount(), request.pin());
        return ResponseEntity.ok(Map.of("msg", "Cash withdrawn successfully"));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) throws Exception {
        accountService.transfer(request.amount(), request.pin(), request.targetAccountNumber());
        return ResponseEntity.ok(Map.of("msg", "Fund transferred successfully"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory() {
        List<TransactionResponse> history = accountService.getTransactionHistory();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/buy-asset")
    public ResponseEntity<?> buyAsset(@Valid @RequestBody BuyAssetRequest request) throws Exception {
        try {
            marketService.buyAsset(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("msg", "Asset purchase successful."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("msg", "Internal error occurred while purchasing the asset."));
        }

    }

    @PostMapping("/sell-asset")
    public ResponseEntity<?> sellAsset(@Valid @RequestBody SellAssetRequest request) throws Exception {
        try {
            marketService.sellAsset(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("msg", "Asset sale successful."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("msg", "Internal error occurred while selling the asset."));
        }

    }

    @GetMapping("/net-worth")
    public ResponseEntity<?> getNetWorth() throws Exception {

        BigDecimal netWorth = marketService.calculateNetWorth();

        if (netWorth == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("msg", "Internal error occurred while fetching net worth."));
        }
        return ResponseEntity.ok(Map.of("netWorth", netWorth));

    }

}
