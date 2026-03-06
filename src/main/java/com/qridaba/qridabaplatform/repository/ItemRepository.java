package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByTitleContainingIgnoreCase(String title);
    List<Item> findByCategoryId(UUID categoryId);
    List<Item> findByOwnerId(UUID ownerId);
}
