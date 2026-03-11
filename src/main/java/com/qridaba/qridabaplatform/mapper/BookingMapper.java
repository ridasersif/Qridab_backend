package com.qridaba.qridabaplatform.mapper;

import com.qridaba.qridabaplatform.model.dto.response.booking.BookingResponse;
import com.qridaba.qridabaplatform.model.entity.booking.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemTitle", source = "item.title")
    @Mapping(target = "renterId", source = "renter.id")
    @Mapping(target = "renterName", expression = "java(booking.getRenter().getFirstName() + \" \" + booking.getRenter().getLastName())")
    BookingResponse toResponse(Booking booking);

}
