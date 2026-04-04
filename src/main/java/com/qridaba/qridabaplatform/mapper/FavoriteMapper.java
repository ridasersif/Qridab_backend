package com.qridaba.qridabaplatform.mapper;

import com.qridaba.qridabaplatform.model.dto.request.FavoriteRequest;
import com.qridaba.qridabaplatform.model.dto.response.FavoriteResponse;
import com.qridaba.qridabaplatform.model.entity.user.Favorite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface FavoriteMapper {

    Favorite toEntity(FavoriteRequest request);

    @Mapping(target = "userId", source = "user.id")
    FavoriteResponse toResponse(Favorite favorite);
}
