package com.riaydev.bankingapp.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.CreatePinRequest;
import com.riaydev.bankingapp.DTO.UpdatePinRequest;
import com.riaydev.bankingapp.Services.PinService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Map;


@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class AccountController {

    private final PinService pinService;

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
    

}
