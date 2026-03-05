package com.qridaba.qridabaplatform.model.entity.user;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Favorite extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}