package com.qridaba.qridabaplatform.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemAvailabilityRequest {
    @NotNull(message = "Date is required")
    private LocalDate unavailableDate;
}
