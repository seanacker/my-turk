package de.mackeprm.myturk.api.auth;

import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

public class LoginResponse {
    @NotNull
    private final String message;
    @Nullable
    private final String balance;
    @Nullable
    private final String token;

    public LoginResponse(String message) {
        this.message = message;
        this.balance = null;
        this.token = null;
    }

    public LoginResponse(String message, String balance, String token) {
        this.message = message;
        this.balance = balance;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    @Nullable
    public String getBalance() {
        return balance;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}
