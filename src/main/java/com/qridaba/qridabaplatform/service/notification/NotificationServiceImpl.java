package com.qridaba.qridabaplatform.service.notification;

import com.qridaba.qridabaplatform.model.entity.enums.NotificationType;
import com.qridaba.qridabaplatform.model.entity.user.Notification;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.NotificationRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.mapper.NotificationMapper;
import com.qridaba.qridabaplatform.model.dto.response.NotificationResponse;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message, NotificationType type,
            String referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", recipient.getEmail(), title);

        // TODO: integrate with WebSocket or push notification service if needed
    }

    @Override
    public List<NotificationResponse> getUserNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Ensure user owns the notification
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this notification.");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, userEmail);
    }
}
