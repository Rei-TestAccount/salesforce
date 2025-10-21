package com.prodly.qa.utils;

import java.util.regex.Pattern;

public class PhoneValidator {
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    public static boolean isValid(String phone) {
        if (phone == null) return false;
        String compact = phone.replaceAll("[\\s\\-()]", "");
        return E164.matcher(compact).matches();
    }
}
