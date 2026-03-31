package com.qridaba.qridabaplatform.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String brand;
    private String model;

    @NotBlank(message = "Condition is required")
    private String itemCondition;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price must be positive")
    private Double pricePerDay;

    private Double deposit;

    private String city;
    private Double latitude;
    private Double longitude;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    private List<String> imageUrls;
}
