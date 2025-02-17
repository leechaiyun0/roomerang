package com.roomerang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "RM_Posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rmPostId;

    @Column(nullable = false, length = 200)
    private String rmBoardTitle;

    @Column(nullable = false, length = 200)
    private String authorName;

    @Column(nullable = false)
    private LocalDateTime postDate = LocalDateTime.now();

    @Column(nullable = false)
    private Integer postViews = 0;

    @Column(nullable = false, length = 200)
    private String authorRegion;

    @Column(nullable = false)
    private Integer authorAge;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 2000)
    private String postContent;

    @Column(nullable = false, length = 100)
    private String userPreference = "일반";

    @Column(nullable = false, length = 10)
    private String authorGender;

    @Column(nullable = true)
    private Integer amount;

    @Column(nullable = false)
    private Integer deposit;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "photo_url", length = 500)
    private List<String> photoUrls = new ArrayList<>();

    @Column(nullable = false, length = 50)
    private String category;
}
