package com.riaydev.bankingapp.DTO;

public record UpdatePinRequest(
    String oldPin,
    String password,
    String newPin
) {

}
