package com.riaydev.bankingapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("token")
        String accessToken
        
) {
}
