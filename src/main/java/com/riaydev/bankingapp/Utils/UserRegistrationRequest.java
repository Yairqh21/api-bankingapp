package com.riaydev.bankingapp.Utils;

import java.util.regex.Pattern;

public class UserRegistrationRequest {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}");

    public boolean validateEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean validatePassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
