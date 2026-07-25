package com.example.forsa.models;

public class AuthenticationResult {

    public enum Status {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        PROFILE_INCOMPLETE
    }

    private final Status status;
    private final long userId;

    public AuthenticationResult(Status status, long userId) {
        this.status = status;
        this.userId = userId;
    }

    public Status getStatus() {
        return status;
    }

    public long getUserId() {
        return userId;
    }
}