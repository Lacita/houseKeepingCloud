package com.houseKeeping.common.config;

import java.util.regex.Pattern;

public class PatternConfig {

    public static boolean isValidPhoneNumber(String phone) {
        if ((phone != null) && (!phone.isEmpty())) {
            boolean matches = Pattern.matches("^1(3[0-9]|5[0-3,5-9]|7[1-3,5-8]|8[0-9])\\d{8}$", phone);
            if (!matches) {
                return true;
            }
        }
        return false;
    }

}
