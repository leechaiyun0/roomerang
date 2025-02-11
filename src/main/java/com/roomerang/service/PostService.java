package com.roomerang.service;

import com.roomerang.entity.Post;
import com.roomerang.repository.PostRepository;
import com.roomerang.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> getPostsByCategory(String category, Pageable pageable) {
        return postRepository.findByCategoryOrderByPostDateDesc(category, pageable);
    }

    @Transactional
    public void savePost(Post post) {
        postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Page<Post> searchPostsByCategory(String category, String keyword, Pageable pageable) {
        return postRepository.findByCategoryAndRmBoardTitleContainingOrCategoryAndAuthorNameContainingOrderByPostDateDesc(
                category, keyword, category, keyword, pageable);
    }

    public void savePostWithCategory(Post post, String category) {
        if ("방 있음".equals(category)) {
            if (post.getAmount() == null) post.setAmount(0);
            if (post.getDeposit() == null) post.setDeposit(0);
        } else {
            post.setAmount(0);
            post.setDeposit(0);
            post.setPhotoUrl(null);
        }
        postRepository.save(post);
    }
}

