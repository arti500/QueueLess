package com.queueless.queueless.dto;

import com.queueless.queueless.entity.QueueEntry.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateEntryStatusRequest(@NotNull Status status) {}
