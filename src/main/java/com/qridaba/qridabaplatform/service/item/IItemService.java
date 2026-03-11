package com.qridaba.qridabaplatform.service.item;

import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface IItemService {
    ItemResponse createItem(ItemRequest request, UUID ownerId, List<MultipartFile> images);

    ItemResponse getItemById(UUID id);

    List<ItemResponse> getAllItems();

    List<ItemResponse> searchItemsByTitle(String title);

    List<ItemResponse> getItemsByCategory(UUID categoryId);

    List<ItemResponse> getItemsByOwner(UUID ownerId);

    ItemResponse updateItem(UUID id, ItemRequest request, UUID ownerId, List<MultipartFile> images);

    void deleteItem(UUID id, UUID ownerId);
}
