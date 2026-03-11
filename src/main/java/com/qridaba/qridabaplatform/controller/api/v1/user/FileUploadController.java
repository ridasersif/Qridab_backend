package com.qridaba.qridabaplatform.controller.api.v1.user;

import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.item.ItemImage;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "File Upload Controller", description = "Endpoints for uploading profile avatars and item images")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // ─── Profile Avatar ───────────────────────────────────────────────────────

    @PostMapping("/avatar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload profile avatar", description = "Uploads a profile picture for the currently authenticated user and saves the URL to their profile.")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String url = fileStorageService.storeFile(file, "avatars");

        // Update user profile avatarUrl
        if (user.getProfile() != null) {
            // Delete old avatar if exists
            fileStorageService.deleteFile(user.getProfile().getAvatarUrl());
            user.getProfile().setAvatarUrl(url);
        }
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    // ─── Item Images ──────────────────────────────────────────────────────────

    @PostMapping("/items/{itemId}/images")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload item image", description = "Uploads an image for a specific item. Use isMain=true to mark it as the main image.")
    public ResponseEntity<Map<String, String>> uploadItemImage(
            @PathVariable UUID itemId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        // Only the item owner can upload images
        if (!item.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't own this item.");
        }

        String url = fileStorageService.storeFile(file, "items");

        // If isMain, unset previous main images
        if (isMain) {
            item.getImages().forEach(img -> img.setMain(false));
        }

        ItemImage image = ItemImage.builder()
                .imageUrl(url)
                .isMain(isMain)
                .item(item)
                .build();

        item.getImages().add(image);
        itemRepository.save(item);

        return ResponseEntity.ok(Map.of("imageUrl", url));
    }

    @DeleteMapping("/items/{itemId}/images")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an item image", description = "Removes a specific image from an item by its URL.")
    public ResponseEntity<Void> deleteItemImage(
            @PathVariable UUID itemId,
            @RequestParam String imageUrl,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't own this item.");
        }

        item.getImages().removeIf(img -> img.getImageUrl().equals(imageUrl));
        fileStorageService.deleteFile(imageUrl);
        itemRepository.save(item);

        return ResponseEntity.noContent().build();
    }
}
