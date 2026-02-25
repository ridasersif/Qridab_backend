package com.qridaba.qridabaplatform.mapper;


import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  RoleMapper {
    Role toEntity(RoleRequest request);
    RoleResponse toResponse(Role role);
}
