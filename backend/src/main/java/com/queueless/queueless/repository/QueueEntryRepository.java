package com.queueless.queueless.repository;

import com.queueless.queueless.entity.QueueEntry;
import com.queueless.queueless.entity.QueueEntry.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {
    List<QueueEntry> findByQueueIdOrderByTokenNumberAsc(Long queueId);
    List<QueueEntry> findByQueueIdAndStatusOrderByTokenNumberAsc(Long queueId, Status status);
    Optional<QueueEntry> findByIdAndQueueId(Long id, Long queueId);
    long countByQueueIdAndStatusIn(Long queueId, List<Status> statuses);
}
