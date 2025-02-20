package com.roomerang.contoller;

import com.roomerang.entity.FavoritePost;
import com.roomerang.service.FavoritePostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/favorite")
public class FavoritePostController {

    private final FavoritePostService favoritePostService;

    public FavoritePostController(FavoritePostService favoritePostService) {
        this.favoritePostService = favoritePostService;
    }

    // 관심글 등록/취소 API (REST API)
    @PostMapping("/toggle")
    public String toggleFavorite(
            @RequestParam String userId,
            @RequestParam Long postId,
            @RequestParam String postType,
            Model model) {

        String message = favoritePostService.toggleFavorite(userId, postId, postType);
        model.addAttribute("message", message);
        return "redirect:/favorite/list"; // 관심글 목록으로 리디렉션
    }

    // 관심글 목록 페이지 반환 (Thymeleaf)
    @GetMapping("/list")
    public String getFavoritePosts(@RequestParam String userId, Model model) {
        List<FavoritePost> favoritePosts = favoritePostService.getFavoritePosts(userId);
        model.addAttribute("userId", userId);
        model.addAttribute("favoritePosts", favoritePosts);
        return "favoriteList"; // Thymeleaf 템플릿 반환
    }
}
