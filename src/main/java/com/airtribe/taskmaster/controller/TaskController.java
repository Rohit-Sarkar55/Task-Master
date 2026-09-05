package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.CreateTaskRequest;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.entities.Task;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long teamId,
                                                                               @Valid @RequestBody CreateTaskRequest request,
                                                                               @AuthenticationPrincipal User currentUser) {
        Task task = taskService.createTask(teamId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.toResponse(task));
    }

    @GetMapping
    public List<TaskResponse> getTeamTasks(@PathVariable Long teamId,
                                           @AuthenticationPrincipal User currentUser) {
        return taskService.getTasksForTeam(teamId, currentUser)
                .stream()
                .map(taskService::toResponse)
                .toList();
    }
}
