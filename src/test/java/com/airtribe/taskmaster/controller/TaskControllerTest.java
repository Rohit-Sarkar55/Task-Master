package com.airtribe.taskmaster.controller;

import com.airtribe.taskmaster.config.SecurityConfig;
import com.airtribe.taskmaster.dto.CommentResponse;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.entities.Comment;
import com.airtribe.taskmaster.entities.Task;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.repositories.UserRepository;
import com.airtribe.taskmaster.security.JwtService;
import com.airtribe.taskmaster.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    // Present only if your SecurityConfig / JwtAuthenticationFilter requires these beans.
    // Remove if not applicable to your project.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private Task testTask;
    private TaskResponse taskResponse;
    private Comment testComment;
    private CommentResponse commentResponse;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.TaskStatus.TODO);

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(Task.TaskStatus.TODO);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test comment");

        commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setContent("Test comment");
    }

    // ---------- POST /api/teams/{teamId}/tasks ----------

    @Test
    void testCreateTask_Success() throws Exception {
        when(taskService.createTask(eq(1L), any(), any(User.class))).thenReturn(testTask);
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);

        mockMvc.perform(post("/api/teams/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void testCreateTask_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/teams/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTask_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/teams/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- PATCH /api/teams/{teamId}/tasks/{taskId}/assign ----------

    @Test
    void testAssignTask_Success() throws Exception {
        when(taskService.assignTask(eq(1L), eq(1L), any(), any(User.class))).thenReturn(testTask);
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/teams/1/tasks/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":2}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testAssignTask_InvalidRequest() throws Exception {
        mockMvc.perform(patch("/api/teams/1/tasks/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAssignTask_Unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/teams/1/tasks/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":2}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- PATCH /api/teams/{teamId}/tasks/{taskId}/status ----------

    @Test
    void testUpdateStatus_Success() throws Exception {
        testTask.setStatus(Task.TaskStatus.IN_PROGRESS);
        taskResponse.setStatus(Task.TaskStatus.IN_PROGRESS);

        when(taskService.updateStatus(eq(1L), eq(1L), any(), any(User.class))).thenReturn(testTask);
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/teams/1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testUpdateStatus_InvalidRequest() throws Exception {
        mockMvc.perform(patch("/api/teams/1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStatus_Unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/teams/1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- GET /api/teams/{teamId}/tasks ----------

    @Test
    void testGetTeamTasks_Success() throws Exception {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(Task.TaskStatus.TODO);

        TaskResponse taskResponse2 = new TaskResponse();
        taskResponse2.setId(2L);
        taskResponse2.setTitle("Task 2");
        taskResponse2.setStatus(Task.TaskStatus.TODO);

        when(taskService.searchTasks(eq(1L), isNull(), isNull(), isNull(), any(Sort.class), any(User.class)))
                .thenReturn(Arrays.asList(testTask, task2));
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);
        when(taskService.toResponse(task2)).thenReturn(taskResponse2);

        mockMvc.perform(get("/api/teams/1/tasks")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    void testGetTeamTasks_WithFilters() throws Exception {
        when(taskService.searchTasks(
                eq(1L),
                eq(Task.TaskStatus.TODO),
                eq(2L),
                eq("search"),
                any(Sort.class),
                any(User.class)))
                .thenReturn(Arrays.asList(testTask));
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/teams/1/tasks")
                        .param("status", "TODO")
                        .param("assignee", "2")
                        .param("q", "search")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void testGetTeamTasks_WithSorting() throws Exception {
        when(taskService.searchTasks(
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                any(Sort.class),
                any(User.class)))
                .thenReturn(Arrays.asList(testTask));
        when(taskService.toResponse(testTask)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/teams/1/tasks")
                        .param("sortBy", "title")
                        .param("direction", "desc")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetTeamTasks_EmptyList() throws Exception {
        when(taskService.searchTasks(
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                any(Sort.class),
                any(User.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/teams/1/tasks")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetTeamTasks_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/teams/1/tasks"))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /api/teams/{teamId}/tasks/{taskId}/comments ----------

    @Test
    void testAddComment_Success() throws Exception {
        when(taskService.addComment(eq(1L), eq(1L), any(), any(User.class))).thenReturn(testComment);
        when(taskService.toCommentResponse(testComment)).thenReturn(commentResponse);

        mockMvc.perform(post("/api/teams/1/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Test comment\"}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void testAddComment_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/teams/1/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddComment_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/teams/1/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Test comment\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- GET /api/teams/{teamId}/tasks/{taskId}/comments ----------

    @Test
    void testGetComments_Success() throws Exception {
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Second comment");

        CommentResponse commentResponse2 = new CommentResponse();
        commentResponse2.setId(2L);
        commentResponse2.setContent("Second comment");

        when(taskService.getComments(eq(1L), eq(1L), any(User.class)))
                .thenReturn(Arrays.asList(testComment, comment2));
        when(taskService.toCommentResponse(testComment)).thenReturn(commentResponse);
        when(taskService.toCommentResponse(comment2)).thenReturn(commentResponse2);

        mockMvc.perform(get("/api/teams/1/tasks/1/comments")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("Test comment"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].content").value("Second comment"));
    }

    @Test
    void testGetComments_EmptyList() throws Exception {
        when(taskService.getComments(eq(1L), eq(1L), any(User.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/teams/1/tasks/1/comments")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetComments_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/teams/1/tasks/1/comments"))
                .andExpect(status().isForbidden());
    }
}