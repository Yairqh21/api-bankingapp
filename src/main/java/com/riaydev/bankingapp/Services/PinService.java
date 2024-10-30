package com.riaydev.bankingapp.Services;


public interface PinService {
     void createPin(String pin, String password);
     void updatePin(String oldPin, String password, String newPin);
       
}

