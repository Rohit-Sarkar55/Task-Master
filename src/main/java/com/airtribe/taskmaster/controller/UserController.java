package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.*;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.NotificationService;
import com.airtribe.taskmaster.service.TaskService;
import com.airtribe.taskmaster.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final TaskService taskService;
    private final NotificationService notificationService;

    public UserController(UserService userService, TaskService taskService, NotificationService notificationService) {
        this.userService = userService;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request)throws BadRequestException {
        String token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return userService.toResponse(currentUser);
    }

    @GetMapping("/me/tasks")
    public List<TaskResponse> myTasks(@AuthenticationPrincipal User currentUser) {
        return taskService.getMyTasks(currentUser.getId())
                .stream()
                .map(taskService::toResponse)
                .toList();
    }

    @GetMapping("/me/notifications")
    public List<NotificationResponse> myNotifications(@AuthenticationPrincipal User currentUser) {
        return notificationService.getForUser(currentUser.getId())
                .stream()
                .map(notificationService::toResponse)
                .toList();
    }

    @PatchMapping("/me/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable Long id,
                                                     @AuthenticationPrincipal User currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok().build();
    }
}
