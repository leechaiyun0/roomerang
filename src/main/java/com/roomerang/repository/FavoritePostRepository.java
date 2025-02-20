package com.roomerang.repository;

import com.roomerang.entity.FavoritePost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, Long> {



}
