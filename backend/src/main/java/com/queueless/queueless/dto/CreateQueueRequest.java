package com.queueless.queueless.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQueueRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 150) String businessName,
        @NotNull @Min(1) Integer maxCapacity
) {}
