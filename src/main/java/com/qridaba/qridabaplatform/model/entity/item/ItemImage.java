package com.qridaba.qridabaplatform.model.entity.item;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemImage extends BaseEntity {
    private String imageUrl;
    private boolean isMain = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}