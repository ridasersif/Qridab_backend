package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.item.ItemAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemAvailabilityRepository extends JpaRepository<ItemAvailability, UUID> {
    List<ItemAvailability> findByItemId(UUID itemId);
}
