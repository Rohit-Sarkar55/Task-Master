package com.airtribe.taskmaster.dto;


import jakarta.validation.constraints.NotNull;

public class AssignTaskRequest {

    @NotNull(message = "assigneeId is required")
    private Long assigneeId;

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
}