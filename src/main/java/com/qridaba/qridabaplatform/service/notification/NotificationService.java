package com.qridaba.qridabaplatform.service.notification;

import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.enums.NotificationType;

import java.util.List;
import java.util.UUID;
import com.qridaba.qridabaplatform.model.dto.response.NotificationResponse;

public interface NotificationService {
    void createNotification(User recipient, String title, String message, NotificationType type, String referenceId);

    List<NotificationResponse> getUserNotifications(String userEmail);

    void deleteNotification(UUID notificationId, String userEmail);
}
