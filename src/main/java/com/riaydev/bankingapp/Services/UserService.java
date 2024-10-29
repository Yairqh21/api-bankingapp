package com.riaydev.bankingapp.Services;

import com.riaydev.bankingapp.DTO.AuthRequest;
import com.riaydev.bankingapp.DTO.TokenDTO;
import com.riaydev.bankingapp.DTO.UserDTO;

public interface UserService {
    UserDTO registerUser(UserDTO user);
    TokenDTO loginUser(AuthRequest request);
    UserDTO getUserInfo(String email);
    String logout(String jwtToken);
}
