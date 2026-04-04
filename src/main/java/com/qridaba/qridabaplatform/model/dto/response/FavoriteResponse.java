package com.qridaba.qridabaplatform.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {
    private UUID id;
    private UUID userId;
    private ItemResponse item;
}
