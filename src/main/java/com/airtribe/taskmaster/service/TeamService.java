package com.airtribe.taskmaster.service;


import com.airtribe.taskmaster.Util.TeamRole;
import com.airtribe.taskmaster.dto.AddMemberRequest;
import com.airtribe.taskmaster.dto.CreateTeamRequest;
import com.airtribe.taskmaster.dto.TeamResponse;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.TeamMember;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.exceptions.BadRequestException;
import com.airtribe.taskmaster.exceptions.ResourceNotFoundException;
import com.airtribe.taskmaster.repositories.TeamMemberRepository;
import com.airtribe.taskmaster.repositories.TeamRepository;
import com.airtribe.taskmaster.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
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
        return teamMemberRepository.findByUserId(userId)
                .stream()
                .map(TeamMember::getTeam)
                .toList();
    }

    public void addMember(Long teamId, AddMemberRequest request, User requestingUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        TeamMember requesterMembership = teamMemberRepository.findByTeamIdAndUserId(teamId, requestingUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this team"));

        if (requesterMembership.getRole() != TeamRole.OWNER && requesterMembership.getRole() != TeamRole.ADMIN) {
            throw new BadRequestException("Only owners or admins can add members");
        }

        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with that email"));

        if (teamMemberRepository.findByTeamIdAndUserId(teamId, userToAdd.getId()).isPresent()) {
            throw new BadRequestException("User is already a member of this team");
        }

        TeamMember newMembership = new TeamMember();
        newMembership.setTeam(team);
        newMembership.setUser(userToAdd);
        newMembership.setRole(TeamRole.MEMBER);
        teamMemberRepository.save(newMembership);
    }
}