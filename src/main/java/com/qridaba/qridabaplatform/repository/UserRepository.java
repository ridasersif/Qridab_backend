package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRoles_Name(String roleName);
    List<User> findAllByDeletedFalse();
}
