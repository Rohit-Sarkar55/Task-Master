package com.airtribe.taskmaster.Util;


import com.airtribe.taskmaster.entities.Task;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {

    public static Specification<Task> belongsToTeam(Long teamId) {
        return (root, query, cb) -> cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Task> hasStatus(Task.TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> titleOrDescriptionContains(String keyword) {
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
        );
    }
}
