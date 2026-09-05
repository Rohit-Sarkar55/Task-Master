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

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Task.TaskStatus getStatus() { return status; }
    public Long getTeamId() { return teamId; }
    public Long getCreatedById() { return createdById; }
    public Long getAssigneeId() { return assigneeId; }
}
