package com.airtribe.taskmaster.controller;

import com.airtribe.taskmaster.config.SecurityConfig;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.dto.UserResponse;
import com.airtribe.taskmaster.entities.Task;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.repositories.UserRepository;
import com.airtribe.taskmaster.security.JwtService;
import com.airtribe.taskmaster.service.TaskService;
import com.airtribe.taskmaster.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    // Present only if your SecurityConfig / JwtAuthenticationFilter requires these beans.
    // Remove if not applicable to your project.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private UserResponse userResponse;
    private Task testTask;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("user@example.com");
        userResponse.setName("Test User");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setStatus(Task.TaskStatus.TODO);

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setStatus(Task.TaskStatus.TODO);
    }

    // ---------- POST /api/users/register ----------

    @Test
    void testRegister_Success() throws Exception {
        when(userService.register(any())).thenReturn(testUser);
        when(userService.toResponse(testUser)).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"user@example.com\",\"password\":\"Password123\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/users/login ----------

    @Test
    void testLogin_Success() throws Exception {
        when(userService.login(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password123\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-jwt-token"));
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /api/users/me ----------

    @Test
    void testMe_Success() throws Exception {
        when(userService.toResponse(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                testUser, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    // ---------- GET /api/users/me/tasks ----------

    @Test
    void testMyTasks_Success() throws Exception {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(Task.TaskStatus.TODO);

        TaskResponse taskResponse2 = new TaskResponse();
        taskResponse2.setId(2L);
        taskResponse2.setTitle("Task 2");
        taskResponse2.setStatus(Task.TaskStatus.TODO);

        when(taskService.getMyTasks(eq(1L))).thenReturn(Arrays.asList(testTask, task2));
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);
        when(taskService.toResponse(task2)).thenReturn(taskResponse2);

        mockMvc.perform(get("/api/users/me/tasks")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                testUser, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    void testMyTasks_EmptyList() throws Exception {
        when(taskService.getMyTasks(eq(1L))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/me/tasks")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                testUser, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testMyTasks_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me/tasks"))
                .andExpect(status().isForbidden());
    }
}