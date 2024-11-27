package com.riaydev.bankingapp.Utils;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class UserRegistrationRequest {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public boolean validateEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }


}
