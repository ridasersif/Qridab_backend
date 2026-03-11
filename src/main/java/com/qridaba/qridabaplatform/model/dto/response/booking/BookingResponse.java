package com.qridaba.qridabaplatform.model.dto.response.booking;

import com.qridaba.qridabaplatform.model.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private UUID id;
    private UUID itemId;
    private String itemTitle;
    private UUID renterId;
    private String renterName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double totalPrice;
    private BookingStatus status;
}
