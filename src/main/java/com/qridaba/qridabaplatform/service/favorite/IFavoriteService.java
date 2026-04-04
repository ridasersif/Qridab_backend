package com.qridaba.qridabaplatform.service.favorite;

import com.qridaba.qridabaplatform.model.dto.request.FavoriteRequest;
import com.qridaba.qridabaplatform.model.dto.response.FavoriteResponse;

import java.util.List;
import java.util.UUID;

public interface IFavoriteService {
    FavoriteResponse addFavorite(FavoriteRequest request, UUID userId);
    void removeFavorite(UUID userId, UUID itemId);
    List<FavoriteResponse> getUserFavorites(UUID userId);
    boolean checkIfFavorite(UUID userId, UUID itemId);
}
