package com.riaydev.bankingapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDTO(
        @JsonProperty("token")
        String accessToken
        
) {
}
