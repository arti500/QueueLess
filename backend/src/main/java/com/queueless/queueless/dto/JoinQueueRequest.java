package com.queueless.queueless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JoinQueueRequest(
        @NotBlank @Size(max = 120) String customerName,
        @Size(max = 30) @Pattern(regexp = "^[0-9+() -]*$", message = "Phone may contain digits, spaces, +, parentheses, and hyphens") String phone
) {}
