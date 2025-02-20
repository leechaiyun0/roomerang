package com.roomerang.repository;

import com.roomerang.entity.FavoritePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, Long> {

    Optional<FavoritePost> findByUserIdAndPostIdAndPostType(String userId, Long postId, String postType);

    List<FavoritePost> findByUserId(String userId);

    void deleteByUserIdAndPostIdAndPostType(String userId, Long postId, String postType);

}
