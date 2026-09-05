package com.airtribe.taskmaster.controller;


import com.airtribe.taskmaster.dto.AddMemberRequest;
import com.airtribe.taskmaster.dto.CreateTeamRequest;
import com.airtribe.taskmaster.dto.TeamResponse;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        Team team = teamService.createTeam(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.toResponse(team));
    }

    @GetMapping
    public List<TeamResponse> getMyTeams(@AuthenticationPrincipal User currentUser) {
        return teamService.getTeamsForUser(currentUser.getId())
                .stream()
                .map(teamService::toResponse)
                .toList();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long id,
                                          @Valid @RequestBody AddMemberRequest request,
                                          @AuthenticationPrincipal User currentUser) {
        teamService.addMember(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}