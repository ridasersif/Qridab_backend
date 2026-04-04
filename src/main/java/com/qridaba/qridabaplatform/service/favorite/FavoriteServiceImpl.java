package com.qridaba.qridabaplatform.service.favorite;

import com.qridaba.qridabaplatform.exception.DuplicateResourceException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.FavoriteMapper;
import com.qridaba.qridabaplatform.model.dto.request.FavoriteRequest;
import com.qridaba.qridabaplatform.model.dto.response.FavoriteResponse;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.user.Favorite;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.FavoriteRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(FavoriteRequest request, UUID userId) {
        if (favoriteRepository.existsByUserIdAndItemId(userId, request.getItemId())) {
            throw new DuplicateResourceException("Item is already in user's favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Favorite favorite = Favorite.builder()
                .user(user)
                .item(item)
                .build();

        favorite = favoriteRepository.save(favorite);
        return favoriteMapper.toResponse(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(UUID userId, UUID itemId) {
        if (!favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }
        favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getUserFavorites(UUID userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(favoriteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIfFavorite(UUID userId, UUID itemId) {
        return favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    }
}
