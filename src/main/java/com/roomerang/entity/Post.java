package com.roomerang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private LocalDateTime postDate;

    @Column(nullable = false)
    private Integer postViews;

    @Column(nullable = false, length = 200)
    private String authorRegion;

    @Column(nullable = false)
    private Integer authorAge;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 2000)
    private String postContent;

    @Column(nullable = false, length = 100)
    private String userPreference;

    @Column(nullable = false, length = 10)
    private String authorGender;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer deposit;

    @Column(nullable = true, length = 500)
    private String photoUrl;

    @Column(nullable = false, length = 50)
    private String category; // 방 있음 / 방 없음
}
