package com.riaydev.bankingapp.Services;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenResponse;
import com.riaydev.bankingapp.DTO.UserRequest;

public interface UserService {
    UserRequest registerUser(UserRequest user) throws Exception;

    TokenResponse loginUser(AuthRequest request);

    UserRequest getUserInfo(String email);

    //String logout(String jwtToken);
}
