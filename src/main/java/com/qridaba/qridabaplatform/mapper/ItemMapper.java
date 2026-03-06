package com.qridaba.qridabaplatform.mapper;

import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemImageResponse;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.item.ItemImage;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Item toEntity(ItemRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", expression = "java(item.getOwner().getFirstName() + \" \" + item.getOwner().getLastName())")
    ItemResponse toResponse(Item item);

    ItemImageResponse toImageResponse(ItemImage image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntityFromRequest(ItemRequest request, @MappingTarget Item item);
}
