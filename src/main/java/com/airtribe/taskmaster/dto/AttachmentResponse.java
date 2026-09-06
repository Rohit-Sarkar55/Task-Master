package com.airtribe.taskmaster.dto;


import java.time.Instant;

public class AttachmentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Long uploadedById;
    private Instant uploadedAt;

    public AttachmentResponse(Long id, String fileName, String fileType, Long uploadedById, Instant uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedById = uploadedById;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public Long getUploadedById() { return uploadedById; }
    public Instant getUploadedAt() { return uploadedAt; }
}
