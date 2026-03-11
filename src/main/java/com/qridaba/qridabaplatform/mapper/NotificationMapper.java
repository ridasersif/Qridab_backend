package com.qridaba.qridabaplatform.mapper;

import com.qridaba.qridabaplatform.model.dto.response.NotificationResponse;
import com.qridaba.qridabaplatform.model.entity.user.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
