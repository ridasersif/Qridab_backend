package com.qridaba.qridabaplatform.model.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    private String phoneNumber;
    private String address;
    private String city;
    private String avatarUrl;

    @OneToOne(mappedBy = "profile")
    @JsonIgnore
    private User user;
}
