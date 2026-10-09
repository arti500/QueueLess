package com.queueless.queueless.service;

import com.queueless.queueless.dto.*;
import com.queueless.queueless.entity.Queue;
import com.queueless.queueless.entity.QueueEntry;
import com.queueless.queueless.entity.QueueEntry.Status;
import com.queueless.queueless.repository.QueueEntryRepository;
import com.queueless.queueless.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueService {
    private static final List<Status> CAPACITY_STATUSES = List.of(Status.WAITING, Status.CALLED);
    private final QueueRepository queueRepository;
    private final QueueEntryRepository entryRepository;
    private final int averageServiceMinutes;

    public QueueService(QueueRepository queueRepository, QueueEntryRepository entryRepository,
                        @Value("${app.average-service-minutes:5}") int averageServiceMinutes) {
        this.queueRepository = queueRepository;
        this.entryRepository = entryRepository;
        this.averageServiceMinutes = Math.max(1, averageServiceMinutes);
    }

    @Transactional
    public QueueResponse create(CreateQueueRequest request) {
        Queue queue = queueRepository.save(new Queue(request.name().trim(), request.businessName().trim(), request.maxCapacity()));
        return toQueueResponse(queue);
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> list() { return queueRepository.findAll().stream().map(this::toQueueResponse).toList(); }

    @Transactional(readOnly = true)
    public QueueResponse get(Long queueId) { return toQueueResponse(findQueue(queueId)); }

    @Transactional
    public QueueEntryResponse join(Long queueId, JoinQueueRequest request) {
        Queue queue = queueRepository.findByIdForUpdate(queueId).orElseThrow(() -> notFound("Queue not found"));
        if (!queue.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This queue is not accepting customers");
        long occupied = entryRepository.countByQueueIdAndStatusIn(queueId, CAPACITY_STATUSES);
        if (occupied >= queue.getMaxCapacity()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This queue is full");
        int token = queue.getNextTokenNumber();
        queue.setNextTokenNumber(token + 1);
        QueueEntry entry = entryRepository.save(new QueueEntry(queue, token, request.customerName().trim(), blankToNull(request.phone())));
        queueRepository.save(queue);
        return toEntryResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<QueueEntryResponse> entries(Long queueId) {
        findQueue(queueId);
        return entryRepository.findByQueueIdOrderByTokenNumberAsc(queueId).stream().map(this::toEntryResponse).toList();
    }

    @Transactional
    public QueueEntryResponse callNext(Long queueId) {
        findQueueForUpdate(queueId);
        QueueEntry next = entryRepository.findByQueueIdAndStatusOrderByTokenNumberAsc(queueId, Status.WAITING)
                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No waiting customers"));
        next.setStatus(Status.CALLED);
        next.setCalledAt(LocalDateTime.now());
        return toEntryResponse(entryRepository.save(next));
    }

    @Transactional
    public QueueEntryResponse updateStatus(Long queueId, Long entryId, Status status) {
        findQueueForUpdate(queueId);
        QueueEntry entry = entryRepository.findByIdAndQueueId(entryId, queueId)
                .orElseThrow(() -> notFound("Queue entry not found"));
        if (status != Status.COMPLETED && status != Status.SKIPPED && status != Status.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be COMPLETED, SKIPPED, or CANCELLED");
        }
        if (entry.getStatus() != Status.WAITING && entry.getStatus() != Status.CALLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This entry has already left the active queue");
        }
        entry.setStatus(status);
        if (status == Status.COMPLETED) entry.setCompletedAt(LocalDateTime.now());
        return toEntryResponse(entryRepository.save(entry));
    }

    private Queue findQueue(Long id) { return queueRepository.findById(id).orElseThrow(() -> notFound("Queue not found")); }
    private Queue findQueueForUpdate(Long id) { return queueRepository.findByIdForUpdate(id).orElseThrow(() -> notFound("Queue not found")); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private QueueResponse toQueueResponse(Queue queue) {
        int waiting = (int) entryRepository.countByQueueIdAndStatusIn(queue.getId(), List.of(Status.WAITING));
        int called = (int) entryRepository.countByQueueIdAndStatusIn(queue.getId(), List.of(Status.CALLED));
        return new QueueResponse(queue.getId(), queue.getName(), queue.getBusinessName(), queue.getMaxCapacity(),
                queue.isActive(), waiting, called, waiting * averageServiceMinutes, queue.getCreatedAt());
    }

    private QueueEntryResponse toEntryResponse(QueueEntry entry) {
        int ahead = (int) entryRepository.findByQueueIdAndStatusOrderByTokenNumberAsc(entry.getQueue().getId(), Status.WAITING)
                .stream().filter(e -> e.getTokenNumber() < entry.getTokenNumber()).count();
        if (entry.getStatus() == Status.CALLED) ahead = 0;
        return new QueueEntryResponse(entry.getId(), entry.getQueue().getId(), entry.getTokenNumber(),
                entry.getCustomerName(), entry.getPhone(), entry.getStatus(), entry.getJoinedAt(),
                entry.getCalledAt(), entry.getCompletedAt(), ahead * averageServiceMinutes);
    }
}
