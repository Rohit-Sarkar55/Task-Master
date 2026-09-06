package com.airtribe.taskmaster.dto;



import com.airtribe.taskmaster.entities.Task;

import java.time.LocalDate;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Task.TaskStatus status;
    private Long teamId;
    private Long createdById;
    private Long assigneeId;

    public TaskResponse(Long id, String title, String description, LocalDate dueDate,
                        Task.TaskStatus status, Long teamId, Long createdById, Long assigneeId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.teamId = teamId;
        this.createdById = createdById;
        this.assigneeId = assigneeId;
    }

    public TaskResponse() {

    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Task.TaskStatus getStatus() { return status; }
    public Long getTeamId() { return teamId; }
    public Long getCreatedById() { return createdById; }
    public Long getAssigneeId() { return assigneeId; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(Task.TaskStatus status) {
        this.status = status;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
