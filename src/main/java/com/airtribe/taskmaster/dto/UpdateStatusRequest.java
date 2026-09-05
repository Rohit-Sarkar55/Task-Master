package com.airtribe.taskmaster.dto;


import com.airtribe.taskmaster.entities.Task;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private Task.TaskStatus status;

    public Task.TaskStatus getStatus() { return status; }
    public void setStatus(Task.TaskStatus status) { this.status = status; }
}
