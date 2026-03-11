package com.qridaba.qridabaplatform.repository;

import com.qridaba.qridabaplatform.model.entity.booking.Booking;
import com.qridaba.qridabaplatform.model.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status IN :statuses " +
            "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findOverlappingBookings(@Param("itemId") UUID itemId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<BookingStatus> statuses);

    List<Booking> findByItemId(UUID itemId);

    List<Booking> findByRenterId(UUID renterId);
}
