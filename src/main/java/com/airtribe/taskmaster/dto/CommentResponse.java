package com.airtribe.taskmaster.dto;


import java.time.Instant;

public class CommentResponse {

    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private Instant createdAt;

    public CommentResponse(Long id, String content, Long authorId, String authorName, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Instant getCreatedAt() { return createdAt; }
}
