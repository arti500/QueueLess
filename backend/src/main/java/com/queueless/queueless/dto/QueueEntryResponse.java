package com.queueless.queueless.dto;

import com.queueless.queueless.entity.QueueEntry.Status;
import java.time.LocalDateTime;

public record QueueEntryResponse(Long id, Long queueId, Integer tokenNumber, String customerName,
                                 String phone, Status status, LocalDateTime joinedAt,
                                 LocalDateTime calledAt, LocalDateTime completedAt,
                                 int estimatedWaitMinutes) {}
