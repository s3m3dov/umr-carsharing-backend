package com.cimhub.authservice.util;

import java.util.regex.Pattern;

public final class EmailNormalizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private EmailNormalizer() {}

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.toLowerCase().trim();
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
