package com.qridaba.qridabaplatform.model.entity.booking;

import com.qridaba.qridabaplatform.model.entity.BaseEntity;
import com.qridaba.qridabaplatform.model.entity.enums.BookingStatus;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking extends BaseEntity {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double totalPrice;

    private String pickupCode;
    private String returnCode;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id")
    private User renter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}
