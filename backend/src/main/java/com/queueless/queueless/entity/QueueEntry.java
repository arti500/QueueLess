package com.queueless.queueless.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries", indexes = {
        @Index(name = "idx_queue_entry_queue_status", columnList = "queue_id,status"),
        @Index(name = "idx_queue_entry_queue_token", columnList = "queue_id,token_number")
})
public class QueueEntry {
    public enum Status { WAITING, CALLED, COMPLETED, SKIPPED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queue_id", nullable = false)
    private Queue queue;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.WAITING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;

    protected QueueEntry() {}
    public QueueEntry(Queue queue, Integer tokenNumber, String customerName, String phone) {
        this.queue = queue; this.tokenNumber = tokenNumber; this.customerName = customerName;
        this.phone = phone; this.status = Status.WAITING;
    }
    @PrePersist void onCreate() { joinedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Queue getQueue() { return queue; }
    public Integer getTokenNumber() { return tokenNumber; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public Status getStatus() { return status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getCalledAt() { return calledAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setStatus(Status status) { this.status = status; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
