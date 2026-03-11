package com.qridaba.qridabaplatform.service.profile;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.model.dto.request.ChangePasswordRequest;
import com.qridaba.qridabaplatform.model.dto.request.UpdateProfileRequest;
import com.qridaba.qridabaplatform.model.dto.response.ProfileResponse;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.user.UserProfile;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileServiceImp implements IProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    public ProfileResponse getProfile(String userEmail) {
        User user = findUserByEmail(userEmail);
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String userEmail, UpdateProfileRequest request, MultipartFile avatar) {
        User user = findUserByEmail(userEmail);

        // Update User fields if provided
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email address is already taken.");
            }
            user.setEmail(request.getEmail());
        }

        // Update UserProfile fields
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
        }
        if (request.getPhoneNumber() != null)
            profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null)
            profile.setAddress(request.getAddress());
        if (request.getCity() != null)
            profile.setCity(request.getCity());

        // Handle avatar upload if provided
        if (avatar != null && !avatar.isEmpty()) {
            // Delete old avatar first
            fileStorageService.deleteFile(profile.getAvatarUrl());
            String avatarUrl = fileStorageService.storeFile(avatar, "avatars");
            profile.setAvatarUrl(avatarUrl);
        }

        user.setProfile(profile);
        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = findUserByEmail(userEmail);

        // Verify old password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        // Check new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private ProfileResponse toProfileResponse(User user) {
        UserProfile profile = user.getProfile();
        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .address(profile != null ? profile.getAddress() : null)
                .city(profile != null ? profile.getCity() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .build();
    }
}
