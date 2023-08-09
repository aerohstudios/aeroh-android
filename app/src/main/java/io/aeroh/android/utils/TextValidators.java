package io.aeroh.android.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextValidators {

    // The same constants can be found in the Aeroh-iOS app in the under mentioned location.
    //
    // Repo - https://www.github.com/aerohstudios/aeroh-ios
    // File Location - Aeroh Link/Constant/AppConstant.swift
    private static final String EMAIL_VALIDATOR = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private static final String PASSWORD_VALIDATOR = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";

    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_VALIDATOR);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean validatePassword(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_VALIDATOR);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
