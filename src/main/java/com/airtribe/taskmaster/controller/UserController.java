package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.LoginRequest;
import com.airtribe.taskmaster.dto.UserRegisterRequest;
import com.airtribe.taskmaster.dto.UserResponse;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request)throws BadRequestException {
        User user = userService.login(request);
        return ResponseEntity.ok(userService.toResponse(user));
    }
}
