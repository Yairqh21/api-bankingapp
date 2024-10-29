package com.riaydev.bankingapp.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.DTO.AccountDTO;
import com.riaydev.bankingapp.DTO.UserDTO;
import com.riaydev.bankingapp.Services.AccountService;
import com.riaydev.bankingapp.Services.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;




@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUserInfo(Authentication authentication) {
        String email = authentication.getName(); 
        UserDTO userInfo = userService.getUserInfo(email);

        return ResponseEntity.ok(userInfo); 
    }

    @GetMapping("/account")
    public ResponseEntity<AccountDTO> getAccountInfo(Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccountInfo(authentication.getName()));
    }
    
    

}
