package com.qridaba.qridabaplatform.service.booking;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.BookingMapper;
import com.qridaba.qridabaplatform.model.dto.request.booking.BookingRequest;
import com.qridaba.qridabaplatform.model.dto.response.booking.BookingResponse;
import com.qridaba.qridabaplatform.model.entity.booking.Booking;
import com.qridaba.qridabaplatform.model.entity.enums.BookingStatus;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.BookingRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImpTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User renter;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;
    private UUID itemId;
    private UUID renterId;
    private UUID ownerId;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        renter = new User();
        renter.setId(renterId);
        renter.setEmail("renter@example.com");
        renter.setFirstName("Renter");

        owner = new User();
        owner.setId(ownerId);
        owner.setEmail("owner@example.com");

        item = new Item();
        item.setId(itemId);
        item.setOwner(owner);
        item.setAvailable(true);
        item.setPricePerDay(100.0);
        item.setMinRentalDays(1);
        item.setTitle("Test Item");

        bookingRequest = new BookingRequest();
        bookingRequest.setItemId(itemId);
        bookingRequest.setStartDate(LocalDateTime.now().plusDays(1));
        bookingRequest.setEndDate(LocalDateTime.now().plusDays(3));

        booking = Booking.builder()
                .item(item)
                .renter(renter)
                .startDate(bookingRequest.getStartDate())
                .endDate(bookingRequest.getEndDate())
                .status(BookingStatus.PENDING)
                .totalPrice(200.0)
                .build();
        booking.setId(bookingId);

        bookingResponse = new BookingResponse();
        bookingResponse.setId(bookingId);
    }

    @Test
    void createBooking_WhenValid_ShouldSaveAndReturnResponse() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findByEmail(renter.getEmail())).thenReturn(Optional.of(renter));
        when(bookingRepository.findOverlappingBookings(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

        // Act
        BookingResponse result = bookingService.createBooking(bookingRequest, renter.getEmail());

        // Assert
        assertThat(result).isNotNull();
        verify(bookingRepository).save(any(Booking.class));
        verify(notificationService).createNotification(eq(owner), anyString(), anyString(), any(), any());
    }

    @Test
    void createBooking_WhenStartDateAfterEndDate_ShouldThrowException() {
        bookingRequest.setEndDate(bookingRequest.getStartDate().minusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, renter.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before end date");
    }

    @Test
    void createBooking_WhenBookingOwnItem_ShouldThrowException() {
        // Renting own item
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, owner.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot book your own item");
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowException() {
        item.setAvailable(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findByEmail(renter.getEmail())).thenReturn(Optional.of(renter));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, renter.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void updateBookingStatus_WhenOwnerAccepts_ShouldUpdateStatus() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

        // Act
        BookingResponse result = bookingService.updateBookingStatus(bookingId, "ACCEPTED", owner.getEmail());

        // Assert
        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(notificationService).createNotification(eq(renter), anyString(), anyString(), eq(com.qridaba.qridabaplatform.model.entity.enums.NotificationType.BOOKING_ACCEPTED), any());
    }

    @Test
    void updateBookingStatus_WhenRenterCancels_ShouldUpdateStatus() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(renter.getEmail())).thenReturn(Optional.of(renter));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

        // Act
        BookingResponse result = bookingService.updateBookingStatus(bookingId, "CANCELLED", renter.getEmail());

        // Assert
        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
}
