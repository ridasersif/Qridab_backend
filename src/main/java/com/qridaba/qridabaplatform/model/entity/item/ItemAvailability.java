package com.qridaba.qridabaplatform.model.entity.item;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "item_availability")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemAvailability extends BaseEntity {
    private LocalDate unavailableDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}