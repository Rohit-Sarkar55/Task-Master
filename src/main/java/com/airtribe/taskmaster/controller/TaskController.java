package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.*;
import com.airtribe.taskmaster.entities.Attachment;
import com.airtribe.taskmaster.entities.Comment;
import com.airtribe.taskmaster.entities.Task;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long teamId,
                                                      @PathVariable Long taskId,
                                                      @Valid @RequestBody AddCommentRequest request,
                                                      @AuthenticationPrincipal User currentUser) {
        Comment comment = taskService.addComment(teamId, taskId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.toCommentResponse(comment));
    }

    @GetMapping("/{taskId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long teamId,
                                             @PathVariable Long taskId,
                                             @AuthenticationPrincipal User currentUser) {
        return taskService.getComments(teamId, taskId, currentUser)
                .stream()
                .map(taskService::toCommentResponse)
                .toList();
    }

    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable Long teamId,
                                                               @PathVariable Long taskId,
                                                               @RequestParam("file") MultipartFile file,
                                                               @AuthenticationPrincipal User currentUser) {
        Attachment attachment = taskService.uploadAttachment(teamId, taskId, file, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.toAttachmentResponse(attachment));
    }

    @GetMapping("/{taskId}/attachments")
    public List<AttachmentResponse> listAttachments(@PathVariable Long teamId,
                                                    @PathVariable Long taskId,
                                                    @AuthenticationPrincipal User currentUser) {
        return taskService.getAttachments(teamId, taskId, currentUser)
                .stream()
                .map(taskService::toAttachmentResponse)
                .toList();
    }

    @GetMapping("/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long teamId,
                                                       @PathVariable Long taskId,
                                                       @PathVariable Long attachmentId,
                                                       @AuthenticationPrincipal User currentUser) throws IOException {
        Attachment attachment = taskService.getAttachmentForDownload(teamId, taskId, attachmentId, currentUser);
        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }
}
