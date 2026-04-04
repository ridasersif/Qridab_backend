package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.user.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByUserIdAndItemId(UUID userId, UUID itemId);

    List<Favorite> findByUserId(UUID userId);

    boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

    void deleteByUserIdAndItemId(UUID userId, UUID itemId);
}