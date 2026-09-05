package com.airtribe.taskmaster.service;


import com.airtribe.taskmaster.Util.TaskSpecifications;
import com.airtribe.taskmaster.dto.AssignTaskRequest;
import com.airtribe.taskmaster.dto.CreateTaskRequest;
import com.airtribe.taskmaster.dto.TaskResponse;
import com.airtribe.taskmaster.dto.UpdateStatusRequest;
import com.airtribe.taskmaster.entities.Team;
import com.airtribe.taskmaster.entities.Task;

import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.exceptions.BadRequestException;
import com.airtribe.taskmaster.exceptions.ResourceNotFoundException;
import com.airtribe.taskmaster.repositories.TaskRepository;
import com.airtribe.taskmaster.repositories.TeamMemberRepository;
import com.airtribe.taskmaster.repositories.TeamRepository;
import com.airtribe.taskmaster.repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
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

    public Task assignTask(Long teamId, Long taskId, AssignTaskRequest request, User currentUser) {
        Task task = getTaskInTeam(teamId, taskId, currentUser);

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        teamMemberRepository.findByTeamIdAndUserId(teamId, assignee.getId())
                .orElseThrow(() -> new BadRequestException("That user is not a member of this team"));

        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    public Task updateStatus(Long teamId, Long taskId, UpdateStatusRequest request, User currentUser) {
        Task task = getTaskInTeam(teamId, taskId, currentUser);
        task.setStatus(request.getStatus());
        return taskRepository.save(task);
    }

    private Task getTaskInTeam(Long teamId, Long taskId, User currentUser) {
        teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this team"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Task not found in this team");
        }

        return task;
    }

    public List<Task> searchTasks(Long teamId, Task.TaskStatus status, Long assigneeId, String query,
                                  Sort sort, User currentUser) {
        teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member of this team"));

        Specification<Task> spec = TaskSpecifications.belongsToTeam(teamId);

        if (status != null) {
            spec = spec.and(TaskSpecifications.hasStatus(status));
        }
        if (assigneeId != null) {
            spec = spec.and(TaskSpecifications.hasAssignee(assigneeId));
        }
        if (query != null && !query.isBlank()) {
            spec = spec.and(TaskSpecifications.titleOrDescriptionContains(query));
        }

        return taskRepository.findAll(spec, sort);
    }
}