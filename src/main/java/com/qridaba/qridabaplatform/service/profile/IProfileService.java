package com.qridaba.qridabaplatform.service.profile;

import com.qridaba.qridabaplatform.model.dto.request.ChangePasswordRequest;
import com.qridaba.qridabaplatform.model.dto.request.UpdateProfileRequest;
import com.qridaba.qridabaplatform.model.dto.response.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    ProfileResponse getProfile(String userEmail);

    ProfileResponse updateProfile(String userEmail, UpdateProfileRequest request, MultipartFile avatar);

    void changePassword(String userEmail, ChangePasswordRequest request);
}
