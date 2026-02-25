package com.qridaba.qridabaplatform.service.auth;

import com.qridaba.qridabaplatform.model.dto.request.*;
import com.qridaba.qridabaplatform.model.dto.response.AuthenticationResponse;
import com.qridaba.qridabaplatform.model.dto.response.MessageResponse;

public interface IAuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(LoginRequest request);

    MessageResponse logout();

    AuthenticationResponse verifyEmail(VerificationRequest request);

    AuthenticationResponse refreshToken(String token);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}

