package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.user.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
    Optional<PendingUser> findByEmail(String email);

    Optional<PendingUser> findByEmailAndVerificationCode(String email, String verificationCode);
}
