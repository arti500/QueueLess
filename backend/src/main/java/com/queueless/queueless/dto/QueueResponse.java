package com.queueless.queueless.dto;

import java.time.LocalDateTime;

public record QueueResponse(Long id, String name, String businessName, Integer maxCapacity,
                            boolean active, int waitingCount, int calledCount,
                            int estimatedWaitMinutes, LocalDateTime createdAt) {}
