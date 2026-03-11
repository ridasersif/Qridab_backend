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
import com.qridaba.qridabaplatform.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request, UUID ownerId, List<MultipartFile> images) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + ownerId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        Item item = itemMapper.toEntity(request);
        item.setOwner(owner);
        item.setCategory(category);

        List<ItemImage> itemImages = new ArrayList<>();

        // Upload files if provided
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    String url = fileStorageService.storeFile(file, "items");
                    itemImages.add(ItemImage.builder()
                            .imageUrl(url)
                            .item(item)
                            .isMain(i == 0) // First image is main
                            .build());
                }
            }
        }

        // Also support passing URLs directly (backward compatibility)
        if (request.getImageUrls() != null) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                itemImages.add(ItemImage.builder()
                        .imageUrl(request.getImageUrls().get(i))
                        .item(item)
                        .isMain(itemImages.isEmpty() && i == 0)
                        .build());
            }
        }

        item.setImages(itemImages);
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
    public ItemResponse updateItem(UUID id, ItemRequest request, UUID ownerId, List<MultipartFile> images) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not the owner of this item");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        itemMapper.updateEntityFromRequest(request, item);
        item.setCategory(category);

        // If new files uploaded, replace all images
        if (images != null && !images.isEmpty()) {
            // Delete old files from storage
            item.getImages().forEach(img -> fileStorageService.deleteFile(img.getImageUrl()));
            item.getImages().clear();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    String url = fileStorageService.storeFile(file, "items");
                    item.getImages().add(ItemImage.builder()
                            .imageUrl(url)
                            .item(item)
                            .isMain(i == 0)
                            .build());
                }
            }
        } else if (request.getImageUrls() != null) {
            item.getImages().clear();
            List<ItemImage> newImages = request.getImageUrls().stream()
                    .map(url -> ItemImage.builder().imageUrl(url).item(item).isMain(false).build())
                    .collect(Collectors.toList());
            if (!newImages.isEmpty())
                newImages.get(0).setMain(true);
            item.getImages().addAll(newImages);
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
