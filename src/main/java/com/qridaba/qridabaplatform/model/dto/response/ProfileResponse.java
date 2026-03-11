package com.qridaba.qridabaplatform.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    // UserProfile fields
    private String phoneNumber;
    private String address;
    private String city;
    private String avatarUrl;
}
