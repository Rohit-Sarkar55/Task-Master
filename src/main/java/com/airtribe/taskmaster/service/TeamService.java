package com.airtribe.taskmaster.service;


import com.airtribe.taskmaster.Util.TeamRole;
import com.airtribe.taskmaster.dto.CreateTeamRequest;
import com.airtribe.taskmaster.dto.TeamResponse;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.TeamMember;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.repositories.TeamMemberRepository;
import com.airtribe.taskmaster.repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public Team createTeam(CreateTeamRequest request, User owner) {
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setOwner(owner);
        Team savedTeam = teamRepository.save(team);

        TeamMember membership = new TeamMember();
        membership.setTeam(savedTeam);
        membership.setUser(owner);
        membership.setRole(TeamRole.OWNER);
        teamMemberRepository.save(membership);

        return savedTeam;
    }

    public TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getOwner().getId()
        );
    }

    public List<Team> getTeamsForUser(Long userId) {
        return teamRepository.findByOwnerId(userId);
    }
}