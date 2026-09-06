package com.airtribe.taskmaster.dto;


import java.time.Instant;

public class NotificationResponse {

    private Long id;
    private String message;
    private Long taskId;
    private boolean isRead;
    private Instant createdAt;

    public NotificationResponse(Long id, String message, Long taskId, boolean isRead, Instant createdAt) {
        this.id = id;
        this.message = message;
        this.taskId = taskId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public Long getTaskId() { return taskId; }
    public boolean isRead() { return isRead; }
    public Instant getCreatedAt() { return createdAt; }
}