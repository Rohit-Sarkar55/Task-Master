package com.airtribe.taskmaster.repositories;


import com.airtribe.taskmaster.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTaskId(Long taskId);
}
