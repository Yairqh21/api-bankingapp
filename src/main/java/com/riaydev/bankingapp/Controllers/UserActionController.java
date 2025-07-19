package com.riaydev.bankingapp.Controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.PinRequest;
import com.riaydev.bankingapp.DTO.SubscriptionRequest;
import com.riaydev.bankingapp.Services.AutoInvestService;
import com.riaydev.bankingapp.Services.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-actions")
@RequiredArgsConstructor
public class UserActionController {

    private final SubscriptionService subscriptionService;
    private final AutoInvestService userActionsService;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        String response = subscriptionService.createSubscription(
                request.amount(),
                request.intervalSeconds(),
                request.pin());

        return ResponseEntity.ok(Map.of("message", response));
    }

    @PostMapping("/enable-auto-invest")
    public ResponseEntity<Map<String, String>> enableAutoInvest(@RequestBody @Valid PinRequest request) {
        if (request.pin() == null || request.pin().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "PIN cannot be null or empty"));
        }

        String response = userActionsService.enableAutoInvest(request.pin());
        return ResponseEntity.ok(Map.of("message", response));
    }

}