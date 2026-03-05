package com.qridaba.qridabaplatform.model.entity.item;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    private String icon;
}