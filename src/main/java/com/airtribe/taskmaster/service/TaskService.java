package com.airtribe.taskmaster.service;


import com.airtribe.taskmaster.dto.CreateTaskRequest;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.Task;

import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.exceptions.BadRequestException;
import com.airtribe.taskmaster.exceptions.ResourceNotFoundException;
import com.airtribe.taskmaster.repositories.TaskRepository;
import com.airtribe.taskmaster.repositories.TeamMemberRepository;
import com.airtribe.taskmaster.repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TaskService(TaskRepository taskRepository, TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository) {
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public Task createTask(Long teamId, CreateTaskRequest request, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this team"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setTeam(team);
        task.setCreatedBy(currentUser);

        return taskRepository.save(task);
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getTeam().getId(),
                task.getCreatedBy() != null ? task.getCreatedBy().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getId() : null
        );
    }

    public List<Task> getTasksForTeam(Long teamId, User currentUser) {
        teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this team"));

        return taskRepository.findByTeamId(teamId);
    }

    public List<Task> getMyTasks(Long userId) {
        return taskRepository.findByAssigneeId(userId);
    }
}