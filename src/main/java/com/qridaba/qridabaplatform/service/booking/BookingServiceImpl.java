package com.qridaba.qridabaplatform.service.booking;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.model.entity.booking.Booking;
import com.qridaba.qridabaplatform.model.entity.enums.BookingStatus;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.dto.request.booking.BookingRequest;
import com.qridaba.qridabaplatform.model.dto.response.booking.BookingResponse;
import com.qridaba.qridabaplatform.mapper.BookingMapper;
import com.qridaba.qridabaplatform.repository.BookingRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.model.entity.enums.NotificationType;
import com.qridaba.qridabaplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())
                || request.getStartDate().isEqual(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Let's prevent user from booking their own item
        if (item.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot book your own item.");
        }

        // Check availability boolean flag
        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Item is currently not available for rent generally.");
        }

        // Calculate calendar days
        long calendarDays = ChronoUnit.DAYS.between(request.getStartDate().toLocalDate(), request.getEndDate().toLocalDate()) + 1;

        // Check overlaps
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.ACCEPTED);
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                item.getId(), request.getStartDate(), request.getEndDate(), activeStatuses);

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("Item is already booked for the selected dates.");
        }

        // Calculate price
        double totalPrice = 0.0;
        if (item.getPricePerDay() != null) {
            long billableDays = Math.max(1, calendarDays);
            totalPrice = item.getPricePerDay() * billableDays;
        }

        // Create booking in DRAFT status as requested
        Booking booking = Booking.builder()
                .item(item)
                .renter(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.PENDING) // Default status is PENDING
                .totalPrice(totalPrice)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Send Notification to Item Owner
        String notificationTitle = "New Booking Request";
        String notificationMessage = "User " + user.getFirstName() + " has requested to book your item: "
                + item.getTitle() + " from " + request.getStartDate() + " to " + request.getEndDate();
        notificationService.createNotification(item.getOwner(), notificationTitle, notificationMessage,
                NotificationType.BOOKING_CREATED, savedBooking.getId().toString());

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public List<LocalDateTime[]> getUnavailableDates(UUID itemId) {
        // Return start and end date pairs for active bookings
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.ACCEPTED);
        return bookingRepository.findByItemId(itemId).stream()
                .filter(b -> activeStatuses.contains(b.getStatus()))
                .map(b -> new LocalDateTime[] { b.getStartDate(), b.getEndDate() })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(UUID bookingId, String newStatus, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Check permission: only the item owner or the renter (if cancelling) can
        // update status
        boolean isOwner = booking.getItem().getOwner().getId().equals(user.getId());
        boolean isRenter = booking.getRenter().getId().equals(user.getId());

        if (!isOwner && !isRenter) {
            throw new IllegalArgumentException("You don't have permission to update this booking.");
        }

        BookingStatus parsedStatus;
        try {
            parsedStatus = BookingStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + newStatus);
        }

        // Validate state transitions
        if (isRenter && parsedStatus != BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Renters can only CANCEL a booking.");
        }

        if (isOwner && parsedStatus == BookingStatus.CANCELLED) {
            // Owner technically rejects or cancels? We'll allow owner to cancel/reject.
            if (booking.getStatus() == BookingStatus.PENDING) {
                parsedStatus = BookingStatus.REJECTED;
            }
        }

        booking.setStatus(parsedStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // Send Notification to Client/Renter if owner updated it
        if (isOwner) {
            String notificationTitle = "Booking Status Updated";
            String notificationMessage = "Your booking for " + booking.getItem().getTitle() + " has been "
                    + parsedStatus.name();

            NotificationType notifType = NotificationType.SYSTEM_ALERT;
            if (parsedStatus == BookingStatus.ACCEPTED)
                notifType = NotificationType.BOOKING_ACCEPTED;
            if (parsedStatus == BookingStatus.REJECTED)
                notifType = NotificationType.BOOKING_REJECTED;
            if (parsedStatus == BookingStatus.CANCELLED)
                notifType = NotificationType.BOOKING_CANCELLED;

            notificationService.createNotification(
                    booking.getRenter(),
                    notificationTitle,
                    notificationMessage,
                    notifType,
                    savedBooking.getId().toString());
        }

        // Send Notification to Owner if Renter cancelled it
        if (isRenter && parsedStatus == BookingStatus.CANCELLED) {
            String notificationTitle = "Booking Cancelled by Client";
            String notificationMessage = "User " + user.getFirstName() + " has cancelled their booking for "
                    + booking.getItem().getTitle();

            notificationService.createNotification(
                    booking.getItem().getOwner(),
                    notificationTitle,
                    notificationMessage,
                    NotificationType.BOOKING_CANCELLED,
                    savedBooking.getId().toString());
        }

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public List<BookingResponse> getBookingsForOwner(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        
        List<Booking> bookings = bookingRepository.findByItemOwnerId(owner.getId());
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}
