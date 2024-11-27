package com.riaydev.bankingapp.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.SubscriptionRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/user-actions")
public class UserActionController {


    @PostMapping("/subscribe")
    public ResponseEntity<?> subscription(@RequestBody SubscriptionRequest sRequest) {
        //TODO: process POST request
        
        return null;
    }

    @PostMapping("/enable-auto-invest/{pin}")
    public ResponseEntity<?> postMethodName(@RequestBody String pin) {
        //TODO: process POST request
        
        return null;
    }
    


}
