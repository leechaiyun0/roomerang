package com.roomerang.service;

import com.roomerang.entity.FavoritePost;
import com.roomerang.entity.Post;
import com.roomerang.entity.SharePost;
import com.roomerang.entity.User;
import com.roomerang.repository.FavoritePostRepository;
import com.roomerang.repository.PostRepository;
import com.roomerang.repository.SharePostRepository;
import com.roomerang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritePostService {

    private final FavoritePostRepository favoritePostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SharePostRepository sharePostRepository;

    @Transactional
    public void toggleFavorite(Long userId, Long postId, Long sharePostId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (postId != null) {
            Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
            if (favoritePostRepository.existsByUserIdAndPost_RmPostId(userId, postId)) {
                favoritePostRepository.deleteByUserIdAndPost_RmPostId(userId, postId);
            } else {
                favoritePostRepository.save(new FavoritePost(user, post));
            }
        } else if (sharePostId != null) {
            SharePost sharePost = sharePostRepository.findById(sharePostId).orElseThrow(() -> new RuntimeException("SharePost not found"));
            if (favoritePostRepository.existsByUserIdAndSharePost_TxnPostId(userId, sharePostId)) {
                favoritePostRepository.deleteByUserIdAndSharePost_TxnPostId(userId, sharePostId);
            } else {
                favoritePostRepository.save(new FavoritePost(user, sharePost));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FavoritePost> getUserFavorites(Long userId) {
        return favoritePostRepository.findAllByUserId(userId);
    }
}
