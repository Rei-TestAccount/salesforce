package com.prodly.qa.utils;

import java.util.Random;

public class PhoneGenerator {
    private static final Random RND = new Random();

    public static String forCountry(String country) {
        switch (country) {
            case "US":  return "+1"  + digits(10);
            case "GB":  return "+44" + digits(10);
            case "DE":  return "+49" + digits(10);
            case "FR":  return "+33" + digits(9);
            case "UA":  return "+380" + digits(9);
            default:    return "+1"  + digits(10);
        }
    }

    private static String digits(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(RND.nextInt(10));
        return sb.toString();
    }
}
