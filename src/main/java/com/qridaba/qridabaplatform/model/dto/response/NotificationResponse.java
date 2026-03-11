package com.qridaba.qridabaplatform.model.dto.response;

import com.qridaba.qridabaplatform.model.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private String referenceId;
    private LocalDateTime createdAt;
}
