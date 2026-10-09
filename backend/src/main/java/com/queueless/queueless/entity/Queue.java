package com.queueless.queueless.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queues")
public class Queue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer nextTokenNumber = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Queue() {}

    public Queue(String name, String businessName, Integer maxCapacity) {
        this.name = name;
        this.businessName = businessName;
        this.maxCapacity = maxCapacity;
        this.nextTokenNumber = 1;
        this.active = true;
    }

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBusinessName() { return businessName; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public boolean isActive() { return active; }
    public Integer getNextTokenNumber() { return nextTokenNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setActive(boolean active) { this.active = active; }
    public void setNextTokenNumber(Integer nextTokenNumber) { this.nextTokenNumber = nextTokenNumber; }
}
