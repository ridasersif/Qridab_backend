package com.qridaba.qridabaplatform.controller.api.booking;

import com.qridaba.qridabaplatform.model.dto.request.booking.BookingRequest;
import com.qridaba.qridabaplatform.model.dto.response.booking.BookingResponse;
import com.qridaba.qridabaplatform.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "Endpoints for managing item reservations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new booking in DRAFT status", description = "Reserves an item for the specified start and end dates if available. The initial status of the booking is DRAFT.")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookingResponse response = bookingService.createBooking(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/item/{itemId}/unavailable-dates")
    @Operation(summary = "Get unavailable dates for an item", description = "Returns a list of date ranges that are already booked for an item")
    public ResponseEntity<List<LocalDateTime[]>> getUnavailableDates(
            @Parameter(description = "ID of the item") @PathVariable UUID itemId) {

        List<LocalDateTime[]> unavailableDates = bookingService.getUnavailableDates(itemId);
        return ResponseEntity.ok(unavailableDates);
    }

    @PutMapping("/{bookingId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update booking status", description = "Allows item owner to accept/reject or client to cancel a booking.")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @Parameter(description = "ID of the booking") @PathVariable UUID bookingId,
            @Parameter(description = "New status (ACCEPTED, REJECTED, CANCELLED, COMPLETED)") @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookingResponse response = bookingService.updateBookingStatus(bookingId, status, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
