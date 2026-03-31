package com.qridaba.qridabaplatform.model.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private UUID id;
    private String title;
    private String description;
    private String brand;
    private String model;
    private String itemCondition;
    private Double pricePerDay;
    private Double deposit;

    private String city;
    private Double latitude;
    private Double longitude;
    private boolean available;
    private UUID categoryId;
    private String categoryName;
    private UUID ownerId;
    private String ownerName;
    private List<ItemImageResponse> images;
}
