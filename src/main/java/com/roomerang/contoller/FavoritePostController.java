package com.roomerang.controller;

import com.roomerang.entity.FavoritePost;
import com.roomerang.service.FavoritePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoritePostController {

    private final FavoritePostService favoritePostService;

    @PostMapping("/toggle")
    @ResponseBody
    public String toggleFavorite(@RequestParam Long userId,
                                 @RequestParam(required = false) Long postId,
                                 @RequestParam(required = false) Long sharePostId) {
        favoritePostService.toggleFavorite(userId, postId, sharePostId);
        return "관심글이 업데이트되었습니다.";
    }

    @GetMapping("/list")
    public String getUserFavorites(@RequestParam Long userId, Model model) {
        List<FavoritePost> favorites = favoritePostService.getUserFavorites(userId);
        model.addAttribute("favorites", favorites);
        return "favoriteList";
    }
}
