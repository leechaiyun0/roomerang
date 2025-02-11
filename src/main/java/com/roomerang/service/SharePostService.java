package com.roomerang.service;

import com.roomerang.entity.SharePost;
import com.roomerang.repository.SharePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SharePostService {

    private final SharePostRepository sharePostRepository;

    public SharePostService(SharePostRepository sharePostRepository) {
        this.sharePostRepository = sharePostRepository;
    }

    //최신순 정렬 적용
    public Page<SharePost> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postDate"));
        return sharePostRepository.findAll(pageable);
    }

    @Transactional
    public SharePost getPostById(Long id) {
        SharePost post = sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        increaseViewCount(id);
        return post;
    }

    @Transactional
    public void increaseViewCount(Long id) {
        SharePost post = sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
        sharePostRepository.save(post);
    }

    public SharePost createPost(SharePost sharePost) {
        if (sharePost.getTxnBoardTitle() == null || sharePost.getTxnBoardTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 제목은 필수 입력값입니다.");
        }
        sharePost.setPostDate(LocalDateTime.now()); // ✅ 현재 시간 설정
        sharePost.setViewCount(0); // ✅ 기본값 0 설정
        return sharePostRepository.save(sharePost);
    }




    @Transactional
    public SharePost updatePost(Long id, SharePost sharePost, String userId) {
        SharePost existingPost = sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // ✅ 본인 확인 (강제 로그인 "testUser" 비교)
        if (!existingPost.getUserId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }

        existingPost.setTxnBoardTitle(sharePost.getTxnBoardTitle());
        existingPost.setTxnBoardContent(sharePost.getTxnBoardContent());
        existingPost.setPrice(sharePost.getPrice());
        existingPost.setLocation(sharePost.getLocation());

        // ✅ 기존 사진 유지
        if (sharePost.getPhotoUrl() != null) {
            existingPost.setPhotoUrl(sharePost.getPhotoUrl());
        }

        return sharePostRepository.save(existingPost);
    }



    @Transactional
    public void deletePost(Long id, String userId) {
        SharePost existingPost = sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!existingPost.getUserId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }

        sharePostRepository.delete(existingPost);
    }

    public List<SharePost> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sharePostRepository.findAll(); // 검색어 없으면 전체 목록 반환
        }
        return sharePostRepository.findByTxnBoardTitleContaining(keyword);
    }

}
