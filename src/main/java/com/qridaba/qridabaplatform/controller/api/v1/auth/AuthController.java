package com.qridaba.qridabaplatform.controller.api.v1.auth;

import com.qridaba.qridabaplatform.model.dto.request.*;
import com.qridaba.qridabaplatform.model.dto.response.AuthenticationResponse;
import com.qridaba.qridabaplatform.model.dto.response.MessageResponse;
import com.qridaba.qridabaplatform.service.auth.AuthenticationServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and account management")
public class AuthController {
 
    private final AuthenticationServiceImp authenticationService;
 
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns an authentication token")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }
 
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns an authentication token")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
 
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the current session/token")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(authenticationService.logout());
    }
 
    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email", description = "Verifies a user's email address using a verification token")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    public ResponseEntity<AuthenticationResponse> verifyEmail(
            @Valid @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(authenticationService.verifyEmail(request));
    }
 
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication token", description = "Returns a new access token using a valid refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }
 
    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset", description = "Sends a password reset link to the user's email")
    @ApiResponse(responseCode = "200", description = "Password reset email sent")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.forgotPassword(request));
    }
 
    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password", description = "Resets the user's password using a valid reset token")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }
}
