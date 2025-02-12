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
    private Long rm_Post_Id;

    @Column(nullable = false, length = 200)
    private String rm_board_title;

    @Column(nullable = false, length = 200)
    private String author_name;

    @Column(nullable = false)
    private LocalDateTime postDate;

    @Column(nullable = false)
    private Integer post_views;

    @Column(nullable = false, length = 200)
    private String author_region;

    @Column(nullable = false)
    private Integer author_age;

    @Column(nullable = false, length = 100)
    private String user_id;

    @Column(nullable = false, length = 2000)
    private String post_content;

    @Column(nullable = false, length = 100)
    private String user_preference;

    @Column(nullable = false, length = 1)
    private String author_gender;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer deposit;

    @Column(nullable = true, length = 500)
    private String photoUrl;

    @Column(nullable = false, length = 50)
    private String category; // 방 있음 / 방 없음
}
