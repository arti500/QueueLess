package com.queueless.queueless.auth.dto;
public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, String username, String role) {}
