package com.qridaba.qridabaplatform.controller.api;

import com.qridaba.qridabaplatform.model.dto.response.NotificationResponse;
import com.qridaba.qridabaplatform.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user notifications", description = "Returns all notifications for the authenticated user, ordered by creation date descending.")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<NotificationResponse> responses = notificationService.getUserNotifications(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{notificationId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a notification", description = "Deletes a specific notification belonging to the authenticated user.")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID of the notification to delete") @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.deleteNotification(notificationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
