package com.qridaba.qridabaplatform.service.notification;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.NotificationMapper;
import com.qridaba.qridabaplatform.model.dto.response.NotificationResponse;
import com.qridaba.qridabaplatform.model.entity.enums.NotificationType;
import com.qridaba.qridabaplatform.model.entity.user.Notification;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.NotificationRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User recipient;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private UUID notificationId = UUID.randomUUID();
    private UUID recipientId = UUID.randomUUID();
    private String email = "user@example.com";

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(recipientId);
        recipient.setEmail(email);

        notification = Notification.builder()
                .recipient(recipient)
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .referenceId("ref123")
                .build();
        notification.setId(notificationId);

        notificationResponse = new NotificationResponse();
        notificationResponse.setId(notificationId);
        notificationResponse.setTitle("Test Notification");
    }

    @Test
    void createNotification_ShouldSaveNotification() {
        notificationService.createNotification(recipient, "New Title", "New Message", NotificationType.BOOKING_CREATED, "ref456");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_WhenUserExists_ShouldReturnList() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)).thenReturn(List.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        List<NotificationResponse> result = notificationService.getUserNotifications(email);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(notificationId);
    }

    @Test
    void deleteNotification_WhenOwner_ShouldDelete() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId, email);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_WhenNotOwner_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, "other@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void deleteNotification_WhenNotFound_ShouldThrowException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, email))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
