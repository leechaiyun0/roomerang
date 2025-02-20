package com.roomerang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FavoritePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 관심글을 등록한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private Post post; // 관심글이 매칭해방 게시글인 경우

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_post_id", nullable = true)
    private SharePost sharePost; // 관심글이 나눔해방 게시글인 경우

    public FavoritePost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.sharePost = null;
    }

    public FavoritePost(User user, SharePost sharePost) {
        this.user = user;
        this.post = null;
        this.sharePost = sharePost;
    }
}
