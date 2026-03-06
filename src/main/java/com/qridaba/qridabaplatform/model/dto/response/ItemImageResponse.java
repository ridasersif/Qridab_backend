package com.qridaba.qridabaplatform.model.dto.response;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemImageResponse {
    private UUID id;
    private String imageUrl;
    private boolean isMain;
}
