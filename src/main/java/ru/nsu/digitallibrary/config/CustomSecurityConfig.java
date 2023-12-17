package ru.nsu.digitallibrary.config;

import org.springframework.beans.factory.annotation.Value;

public class CustomSecurityConfig {

    @Value("${secret-word}")
    public static final String secretWord = "chucha-digital-library";
    public static final long accessTokenLifetime = 1000 * 60 * 15;
    public static final long refreshTokenLifetime = 1000 * 60 * 60 * 24;
}

