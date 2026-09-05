package com.airtribe.taskmaster.repositories;


import com.airtribe.taskmaster.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTeamId(Long teamId);

    List<Task> findByAssigneeId(Long assigneeId);
}
