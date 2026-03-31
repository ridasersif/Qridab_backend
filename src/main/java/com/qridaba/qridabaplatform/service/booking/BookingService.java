package com.qridaba.qridabaplatform.service.booking;

import com.qridaba.qridabaplatform.model.dto.request.booking.BookingRequest;
import com.qridaba.qridabaplatform.model.dto.response.booking.BookingResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, String userEmail);

    List<LocalDateTime[]> getUnavailableDates(UUID itemId);

    BookingResponse updateBookingStatus(UUID bookingId, String status, String userEmail);

    List<BookingResponse> getBookingsForOwner(String userEmail);
}
