package com.roomerang.service;

import com.roomerang.entity.FavoritePost;
import com.roomerang.repository.FavoritePostRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class FavoritePostService {

    private final FavoritePostRepository favoritePostRepository;

    public FavoritePostService(FavoritePostRepository favoritePostRepository) {
        this.favoritePostRepository = favoritePostRepository;
    }

    @Transactional
    public String toggleFavorite(String userId, Long postId, String postType) {
        log.info("toggleFavorite 호출됨 - userId: {}, postId: {}, postType: {}", userId, postId, postType);

        Optional<FavoritePost> favorite = favoritePostRepository.findByUserIdAndPostIdAndPostType(userId, postId, postType);

        if (favorite.isPresent()) {
            log.info("관심글 삭제 - userId: {}, postId: {}", userId, postId);
            favoritePostRepository.deleteByUserIdAndPostIdAndPostType(userId, postId, postType);
            return "등록 취소되었습니다.";
        } else {
            FavoritePost newFavorite = new FavoritePost();
            newFavorite.setUserId(userId);
            newFavorite.setPostId(postId);
            newFavorite.setPostType(postType);
            favoritePostRepository.save(newFavorite);
            return "등록되었습니다.";
        }
    }

    public List<FavoritePost> getFavoritePosts(String userId) {
        return favoritePostRepository.findByUserId(userId);
    }
}
