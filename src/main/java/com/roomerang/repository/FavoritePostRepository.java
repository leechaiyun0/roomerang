package com.roomerang.repository;

import com.roomerang.entity.FavoritePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, Long> {

    @Query("SELECT f FROM FavoritePost f LEFT JOIN FETCH f.post p LEFT JOIN FETCH f.sharePost s WHERE f.user.id = :userId")
    List<FavoritePost> findAllByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndPost_RmPostId(Long userId, Long postId);

    boolean existsByUserIdAndSharePost_TxnPostId(Long userId, Long sharePostId);

    void deleteByUserIdAndPost_RmPostId(Long userId, Long postId);

    void deleteByUserIdAndSharePost_TxnPostId(Long userId, Long sharePostId);
}
