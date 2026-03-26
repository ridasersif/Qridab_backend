package com.qridaba.qridabaplatform.service.profile;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.model.dto.request.ChangePasswordRequest;
import com.qridaba.qridabaplatform.model.dto.request.UpdateProfileRequest;
import com.qridaba.qridabaplatform.model.dto.response.ProfileResponse;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.user.UserProfile;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProfileServiceImp profileService;

    private User user;
    private UserProfile profile;
    private String email = "test@example.com";
    private UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        profile = new UserProfile();
        profile.setPhoneNumber("123456789");
        profile.setAddress("123 Street");
        profile.setCity("City");
        profile.setAvatarUrl("old-avatar.jpg");

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("encodedPassword")
                .build();
        user.setId(userId);
        user.setProfile(profile);
    }

    @Test
    void getProfile_WhenUserExists_ShouldReturnResponse() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ProfileResponse response = profileService.getProfile(email);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getPhoneNumber()).isEqualTo("123456789");
    }

    @Test
    void getProfile_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_WhenValidRequest_ShouldUpdateAndReturnResponse() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhoneNumber("987654321");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        ProfileResponse response = profileService.updateProfile(email, request, null);

        // Assert
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getPhoneNumber()).isEqualTo("987654321");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_WhenAvatarProvided_ShouldReplaceAvatar() {
        // Arrange
        MultipartFile avatar = mock(MultipartFile.class);
        UpdateProfileRequest request = new UpdateProfileRequest();
        
        when(avatar.isEmpty()).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(avatar, "avatars")).thenReturn("new-avatar.jpg");

        // Act
        profileService.updateProfile(email, request, avatar);

        // Assert
        verify(fileStorageService).deleteFile("old-avatar.jpg");
        verify(fileStorageService).storeFile(avatar, "avatars");
        assertThat(user.getProfile().getAvatarUrl()).isEqualTo("new-avatar.jpg");
    }

    @Test
    void changePassword_WhenValid_ShouldUpdatePassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmNewPassword("newPass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPassword");

        // Act
        profileService.changePassword(email, request);

        // Assert
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WhenCurrentPasswordIncorrect_ShouldThrowException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> profileService.changePassword(email, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorrect");
    }
}
