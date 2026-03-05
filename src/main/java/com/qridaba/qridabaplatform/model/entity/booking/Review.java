package com.qridaba.qridabaplatform.model.entity.booking;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review extends BaseEntity {
    private Integer rating; // 1 to 5
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}