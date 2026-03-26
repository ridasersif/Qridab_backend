package com.qridaba.qridabaplatform.service.item;

import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.mapper.ItemMapper;
import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImpTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ItemServiceImp itemService;

    private User owner;
    private Category category;
    private Item item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;
    private UUID itemId;
    private UUID ownerId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        owner = new User();
        owner.setId(ownerId);

        category = new Category();
        category.setId(categoryId);

        item = new Item();
        item.setId(itemId);
        item.setOwner(owner);
        item.setCategory(category);
        item.setTitle("Camera");

        itemRequest = new ItemRequest();
        itemRequest.setTitle("Camera");
        itemRequest.setCategoryId(categoryId);

        itemResponse = new ItemResponse();
        itemResponse.setId(itemId);
        itemResponse.setTitle("Camera");
    }

    @Test
    void createItem_WhenOwnerAndCategoryExist_ShouldReturnItemResponse() {
        // Arrange
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(itemMapper.toEntity(itemRequest)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        // Act
        ItemResponse result = itemService.createItem(itemRequest, ownerId, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Camera");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnResponse() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        // Act
        ItemResponse result = itemService.getItemById(itemId);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void updateItem_WhenRequesterIsOwner_ShouldUpdateAndReturnResponse() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        // Act
        ItemResponse result = itemService.updateItem(itemId, itemRequest, ownerId, null);

        // Assert
        assertThat(result).isNotNull();
        verify(itemMapper).updateEntityFromRequest(itemRequest, item);
    }

    @Test
    void updateItem_WhenRequesterIsNotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThatThrownBy(() -> itemService.updateItem(itemId, itemRequest, otherUserId, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteItem_WhenRequesterIsOwner_ShouldCallDelete() {
        // Arrange
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        itemService.deleteItem(itemId, ownerId);

        // Assert
        verify(itemRepository).delete(item);
    }

    @Test
    void searchItemsByTitle_ShouldReturnList() {
        // Arrange
        when(itemRepository.findByTitleContainingIgnoreCase("Cam")).thenReturn(List.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        // Act
        List<ItemResponse> result = itemService.searchItemsByTitle("Cam");

        // Assert
        assertThat(result).hasSize(1);
    }
}
