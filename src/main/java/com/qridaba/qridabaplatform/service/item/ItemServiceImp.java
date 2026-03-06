package com.qridaba.qridabaplatform.service.item;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.ItemMapper;
import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.item.ItemImage;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImp implements IItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + ownerId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        Item item = itemMapper.toEntity(request);
        item.setOwner(owner);
        item.setCategory(category);

        if (request.getImageUrls() != null) {
            List<ItemImage> images = request.getImageUrls().stream()
                    .map(url -> ItemImage.builder()
                            .imageUrl(url)
                            .item(item)
                            .isMain(false) // Can be refined later
                            .build())
                    .collect(Collectors.toList());
            if (!images.isEmpty()) {
                images.get(0).setMain(true);
            }
            item.setImages(images);
        }

        Item savedItem = itemRepository.save(item);
        return itemMapper.toResponse(savedItem);
    }

    @Override
    public ItemResponse getItemById(UUID id) {
        return itemRepository.findById(id)
                .map(itemMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponse> searchItemsByTitle(String title) {
        return itemRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponse> getItemsByCategory(UUID categoryId) {
        return itemRepository.findByCategoryId(categoryId).stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponse> getItemsByOwner(UUID ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse updateItem(UUID id, ItemRequest request, UUID ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not the owner of this item");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        itemMapper.updateEntityFromRequest(request, item);
        item.setCategory(category);

        // Update images (simplified: replace all)
        if (request.getImageUrls() != null) {
            item.getImages().clear();
            List<ItemImage> images = request.getImageUrls().stream()
                    .map(url -> ItemImage.builder()
                            .imageUrl(url)
                            .item(item)
                            .isMain(false)
                            .build())
                    .collect(Collectors.toList());
            if (!images.isEmpty()) {
                images.get(0).setMain(true);
            }
            item.getImages().addAll(images);
        }

        Item updatedItem = itemRepository.save(item);
        return itemMapper.toResponse(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(UUID id, UUID ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not the owner of this item");
        }

        itemRepository.delete(item);
    }
}
