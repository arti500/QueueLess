package com.queueless.queueless.controller;

import com.queueless.queueless.dto.*;
import com.queueless.queueless.entity.QueueEntry.Status;
import com.queueless.queueless.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/queues")
public class QueueController {
    private final QueueService queueService;
    public QueueController(QueueService queueService) { this.queueService = queueService; }

    @GetMapping public List<QueueResponse> list() { return queueService.list(); }
    @PostMapping public QueueResponse create(@Valid @RequestBody CreateQueueRequest request) { return queueService.create(request); }
    @GetMapping("/{queueId}") public QueueResponse get(@PathVariable Long queueId) { return queueService.get(queueId); }
    @GetMapping("/{queueId}/entries") public List<QueueEntryResponse> entries(@PathVariable Long queueId) { return queueService.entries(queueId); }
    @PostMapping("/{queueId}/join") public QueueEntryResponse join(@PathVariable Long queueId, @Valid @RequestBody JoinQueueRequest request) { return queueService.join(queueId, request); }
    @PostMapping("/{queueId}/call-next") public QueueEntryResponse callNext(@PathVariable Long queueId) { return queueService.callNext(queueId); }
    @PatchMapping("/{queueId}/entries/{entryId}/status")
    public QueueEntryResponse updateStatus(@PathVariable Long queueId, @PathVariable Long entryId,
                                           @Valid @RequestBody UpdateEntryStatusRequest request) {
        return queueService.updateStatus(queueId, entryId, request.status());
    }
}
