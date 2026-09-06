package com.airtribe.taskmaster.controller;

import com.airtribe.taskmaster.config.SecurityConfig;
import com.airtribe.taskmaster.dto.TeamResponse;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.repositories.UserRepository;
import com.airtribe.taskmaster.security.JwtService;
import com.airtribe.taskmaster.service.TeamService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@Import(SecurityConfig.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    // Present only if your SecurityConfig / JwtAuthenticationFilter requires these beans.
    // Remove if not applicable to your project.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private Team testTeam;
    private TeamResponse teamResponse;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");

        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("Test Team");

        teamResponse = new TeamResponse();
        teamResponse.setId(1L);
        teamResponse.setName("Test Team");
    }

    // ---------- POST /api/teams ----------

    @Test
    void testCreateTeam_Success() throws Exception {
        when(teamService.createTeam(any(), any(User.class))).thenReturn(testTeam);
        when(teamService.toResponse(testTeam)).thenReturn(teamResponse);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Team\"}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Team"));
    }

    @Test
    void testCreateTeam_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTeam_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Team\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- GET /api/teams ----------

    @Test
    void testGetMyTeams_Success() throws Exception {
        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("Second Team");

        TeamResponse teamResponse2 = new TeamResponse();
        teamResponse2.setId(2L);
        teamResponse2.setName("Second Team");

        when(teamService.getTeamsForUser(eq(1L))).thenReturn(Arrays.asList(testTeam, team2));
        when(teamService.toResponse(testTeam)).thenReturn(teamResponse);
        when(teamService.toResponse(team2)).thenReturn(teamResponse2);

        mockMvc.perform(get("/api/teams")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Team"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Second Team"));
    }

    @Test
    void testGetMyTeams_EmptyList() throws Exception {
        when(teamService.getTeamsForUser(eq(1L))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/teams")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyTeams_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /api/teams/{id}/members ----------

    @Test
    void testAddMember_Success() throws Exception {
        doNothing().when(teamService).addMember(eq(1L), any(), any(User.class));

        mockMvc.perform(post("/api/teams/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"newmember@example.com\"}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(teamService).addMember(eq(1L), any(), any(User.class));
    }

    @Test
    void testAddMember_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/teams/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(authentication(authToken()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddMember_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/teams/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}