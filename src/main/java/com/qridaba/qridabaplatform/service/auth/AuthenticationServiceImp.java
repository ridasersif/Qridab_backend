package com.qridaba.qridabaplatform.service.auth;

import com.qridaba.qridabaplatform.exception.InvalidVerificationException;
import com.qridaba.qridabaplatform.exception.RoleNotAllowedException;
import com.qridaba.qridabaplatform.exception.TokenExpiredException;
import com.qridaba.qridabaplatform.model.dto.response.AuthenticationResponse;
import com.qridaba.qridabaplatform.model.dto.response.MessageResponse;
import com.qridaba.qridabaplatform.model.dto.request.LoginRequest;
import com.qridaba.qridabaplatform.model.dto.request.RegisterRequest;
import com.qridaba.qridabaplatform.mapper.UserMapper;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.exception.EmailAlreadyExistsException;
import com.qridaba.qridabaplatform.model.dto.request.ForgotPasswordRequest;
import com.qridaba.qridabaplatform.model.dto.request.ResetPasswordRequest;
import com.qridaba.qridabaplatform.model.dto.request.VerificationRequest;
import com.qridaba.qridabaplatform.model.entity.user.PendingUser;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.user.UserProfile;
import com.qridaba.qridabaplatform.repository.PendingUserRepository;
import com.qridaba.qridabaplatform.repository.RefreshTokenRepository;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.model.entity.user.RefreshToken;
import com.qridaba.qridabaplatform.service.jwt.JwtServiceImp;
import com.qridaba.qridabaplatform.util.MailService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements IAuthenticationService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtServiceImp jwtService;
        private final AuthenticationManager authenticationManager;
        private final UserMapper userMapper;
        private final PendingUserRepository pendingUserRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final MailService mailService;

        @Value("${application.security.jwt.refresh-token.expiration}")
        private long refreshExpiration;

        @Transactional
        public AuthenticationResponse register(RegisterRequest request) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new EmailAlreadyExistsException("Email already exists");
                }

                UUID requestedRoleId = request.getRoleId();
                Role role = roleRepository.findById(requestedRoleId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Role not found with id: " + requestedRoleId));

                if (role.getName().equalsIgnoreCase("ADMIN") || role.getName().equalsIgnoreCase("SUPER_ADMIN")) {
                        throw new RoleNotAllowedException("Registration for this role is not permitted");
                }

                String code = generateVerificationCode();

                pendingUserRepository.findByEmail(request.getEmail())
                        .ifPresent(pending -> {
                                pendingUserRepository.delete(pending);
                                pendingUserRepository.flush();
                        });

                PendingUser pendingUser = PendingUser.builder()
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .verificationCode(code)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusMinutes(15))
                        .roleId(request.getRoleId())
                        .build();

                pendingUserRepository.save(pendingUser);
                mailService.sendVerificationEmail(pendingUser.getEmail(), code);

                return AuthenticationResponse.builder()
                        .message("Registration successful. Please check your email for the verification code.")
                        .build();
        }

        @Transactional
        public AuthenticationResponse authenticate(LoginRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
                try{
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        request.getEmail(),
                                        request.getPassword()));
                }catch(BadCredentialsException e){
                        throw new BadCredentialsException("Incorrect password");
                }

                Map<String, Object> extraClaims = Map.of(
                        "roles", user.getRoles().stream().map(Role::getName).toList(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName());

                String jwtToken = jwtService.generateToken(extraClaims, user);
                String refreshToken = createRefreshToken(user).getToken();
                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .user(userMapper.toResponse(user))
                        .build();
        }

        @Transactional
        public MessageResponse logout() {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof User user) {
                        refreshTokenRepository.deleteByUser(user);
                }
                SecurityContextHolder.clearContext();
                return MessageResponse.builder()
                        .message("Logout successful")
                        .build();
        }

        @Transactional
        public AuthenticationResponse verifyEmail(VerificationRequest request) {
                PendingUser pendingUser = pendingUserRepository
                        .findByEmailAndVerificationCode(request.getEmail(), request.getCode())
                        .orElseThrow(() -> new InvalidVerificationException("Invalid verification code or email"));

                if (pendingUser.getExpiresAt().isBefore(LocalDateTime.now())) {
                        pendingUserRepository.delete(pendingUser);
                        throw new InvalidVerificationException("Verification code has expired");
                }

                Role userRole = roleRepository.findById(pendingUser.getRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Role not found with id: " + pendingUser.getRoleId()));

                User user = User.builder()
                        .email(pendingUser.getEmail())
                        .password(pendingUser.getPassword())
                        .firstName(pendingUser.getFirstName())
                        .lastName(pendingUser.getLastName())
                        .enabled(true)
                        .roles(Set.of(userRole))
                        .build();

                UserProfile profile = new UserProfile();
                profile.setUser(user);
                user.setProfile(profile);

                User savedUser = userRepository.save(user);
                pendingUserRepository.delete(pendingUser);

                Map<String, Object> extraClaims = Map.of(
                        "roles", savedUser.getRoles().stream().map(Role::getName).toList(),
                        "firstName", savedUser.getFirstName(),
                        "lastName", savedUser.getLastName());

                String jwtToken = jwtService.generateToken(extraClaims, savedUser);
                String refreshToken = createRefreshToken(savedUser).getToken();

                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .user(userMapper.toResponse(savedUser))
                        .build();
        }

        @Transactional
        public AuthenticationResponse refreshToken(String token) {
                RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                        .orElseThrow(() -> new InvalidVerificationException("Refresh token not found"));

                if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(refreshToken);
                        throw new TokenExpiredException("Refresh token was expired. Please make a new signin request");
                }

                User user = refreshToken.getUser();
                Map<String, Object> extraClaims = Map.of(
                        "roles", user.getRoles().stream().map(Role::getName).toList(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName());

                String accessToken = jwtService.generateToken(extraClaims, user);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .user(userMapper.toResponse(user))
                        .build();
        }

        @Transactional
        public MessageResponse forgotPassword(ForgotPasswordRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

                String code = generateVerificationCode();
                user.setResetPasswordCode(code);
                user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);

                mailService.sendVerificationEmail(user.getEmail(), code);

                return MessageResponse.builder()
                        .message("Password reset code sent to your email")
                        .build();
        }

        @Transactional
        public MessageResponse resetPassword(ResetPasswordRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (user.getResetPasswordCode() == null || !user.getResetPasswordCode().equals(request.getCode())) {
                        throw new InvalidVerificationException("Invalid reset code");
                }

                if (user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new InvalidVerificationException("Reset code has expired");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setResetPasswordCode(null);
                user.setResetPasswordExpiresAt(null);
                userRepository.save(user);

                return MessageResponse.builder()
                        .message("Password reset successfully")
                        .build();
        }

        private RefreshToken createRefreshToken(User user) {
                refreshTokenRepository.deleteByUser(user);
                refreshTokenRepository.flush();

                RefreshToken refreshToken = RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(Instant.now().plusMillis(refreshExpiration))
                        .build();

                return refreshTokenRepository.save(refreshToken);
        }

        private String generateVerificationCode() {
                Random random = new Random();
                int code = 100000 + random.nextInt(900000);
                return String.valueOf(code);
        }
}