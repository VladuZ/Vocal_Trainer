package com.example.testing;
public class ChangeResponse {
    private String newValue;
    private String token;

    public ChangeResponse(String newValue, String token) {
        this.newValue = newValue;
        this.token = token;
    }

    public String getValue() {
        return newValue;
    }

    public String getToken() {
        return token;
    }
}

