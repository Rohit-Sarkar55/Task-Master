package com.airtribe.taskmaster.service;



import com.airtribe.taskmaster.dto.NotificationResponse;
import com.airtribe.taskmaster.entities.Notification;
import com.airtribe.taskmaster.entities.User;
import com.airtribe.taskmaster.exceptions.BadRequestException;
import com.airtribe.taskmaster.exceptions.ResourceNotFoundException;
import com.airtribe.taskmaster.repositories.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUser(User user, String message, Long taskId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setTaskId(taskId);
        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = toResponse(saved);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", response);
    }

    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getTaskId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot modify another user's notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
