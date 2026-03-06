package com.qridaba.qridabaplatform.model.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemAvailabilityResponse {
    private UUID id;
    private LocalDate unavailableDate;
}
