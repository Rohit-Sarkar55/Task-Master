package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.AssignTaskRequest;
import com.airtribe.taskmaster.dto.CreateTaskRequest;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.dto.UpdateStatusRequest;
import com.airtribe.taskmaster.entities.Task;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
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

//    @GetMapping
//    public List<TaskResponse> getTeamTasks(@PathVariable Long teamId,
//                                           @AuthenticationPrincipal User currentUser) {
//        return taskService.getTasksForTeam(teamId, currentUser)
//                .stream()
//                .map(taskService::toResponse)
//                .toList();
//    }

    @PatchMapping("/{taskId}/assign")
    public TaskResponse assignTask(@PathVariable Long teamId,
                                   @PathVariable Long taskId,
                                   @Valid @RequestBody AssignTaskRequest request,
                                   @AuthenticationPrincipal User currentUser) {
        Task task = taskService.assignTask(teamId, taskId, request, currentUser);
        return taskService.toResponse(task);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable Long teamId,
                                     @PathVariable Long taskId,
                                     @Valid @RequestBody UpdateStatusRequest request,
                                     @AuthenticationPrincipal User currentUser) {
        Task task = taskService.updateStatus(teamId, taskId, request, currentUser);
        return taskService.toResponse(task);
    }

    @GetMapping
    public List<TaskResponse> getTeamTasks(@PathVariable Long teamId,
                                           @RequestParam(required = false) Task.TaskStatus status,
                                           @RequestParam(required = false) Long assignee,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                           @RequestParam(required = false, defaultValue = "asc") String direction,
                                           @AuthenticationPrincipal User currentUser) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        return taskService.searchTasks(teamId, status, assignee, q, sort, currentUser)
                .stream()
                .map(taskService::toResponse)
                .toList();
    }
}
