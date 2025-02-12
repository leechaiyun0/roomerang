package com.roomerang.contoller;

import com.roomerang.entity.SharePost;
import com.roomerang.service.SharePostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

        @Controller
        @RequestMapping("/share")
        public class SharePostController {

            private final SharePostService sharePostService;

            public SharePostController(SharePostService sharePostService) {
                this.sharePostService = sharePostService;
            }

            // ✅ 게시글 목록 조회
            @GetMapping("/list")
            public String getAllPosts(@RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                      Model model) {
                Page<SharePost> postPage = sharePostService.getAllPosts(page, size);

                model.addAttribute("postPage", postPage);
                model.addAttribute("posts", postPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", postPage.getTotalPages());

                return "share/sharePostList";
            }

            // ✅ 특정 게시글 조회
            @GetMapping("/{id}")
            public String getPostById(@PathVariable Long id, Model model) {
                SharePost post = sharePostService.getPostById(id);
                model.addAttribute("post", post);
                return "share/sharePostDetail"; // 게시글 상세 페이지로 이동
            }


            // ✅ 게시글 작성 페이지 이동
            @GetMapping("/create")
            public String showCreateForm(Model model) {
                model.addAttribute("post", new SharePost());
                return "share/sharePostCreate";
            }

            // ✅ 게시글 등록 (로그인 기능 없이 "testUser"로 자동 저장)
            @PostMapping("/create")
            public String createPost(@ModelAttribute SharePost sharePost,
                                     @RequestParam("photo") MultipartFile photo) throws IOException {

                // 임시 로그인 사용자 정보 설정
                sharePost.setAuthorName("테스트 사용자"); // 필수 값 설정 (임의의 값)
                sharePost.setUserId("testUser"); // 필수 값 설정 (임의의 값)

                if (!photo.isEmpty()) {
                    String uploadDir = "C:/uploads/";
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    File file = new File(uploadDir + fileName);
                    photo.transferTo(file);

                    // DB에 저장할 파일 경로
                    sharePost.setPhotoUrl("/uploads/" + fileName);
                }


                sharePostService.createPost(sharePost);
                return "redirect:/share/list";
            }


            // ✅ 게시글 수정 페이지 이동 (본인만 가능)
            @GetMapping("/{id}/update")
            public String showUpdateForm(@PathVariable Long id, Model model) {
                SharePost post = sharePostService.getPostById(id);

                // 🔥 로그인 기능 없이 "testUser"만 수정 가능하도록 강제
                if (!post.getUserId().equals("testUser")) {
                    throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
                }

                model.addAttribute("post", post);
                return "share/sharePostUpdate";
            }

            // ✅ 게시글 수정 (본인만 가능)
            @PostMapping("/{id}/update")
            public String updatePost(@PathVariable Long id,
                                     @ModelAttribute SharePost sharePost,
                                     @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {

                // 기존 게시글 불러오기
                SharePost existingPost = sharePostService.getPostById(id);

                // 새로운 파일이 업로드되었을 경우만 저장
                if (photo != null && !photo.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    String uploadDir = "C:/uploads/";
                    File file = new File(uploadDir + fileName);
                    photo.transferTo(file);
                    sharePost.setPhotoUrl("/uploads/" + fileName); // 새 이미지 저장
                } else {
                    sharePost.setPhotoUrl(existingPost.getPhotoUrl()); // 기존 이미지 유지
                }

                // 게시글 업데이트
                sharePostService.updatePost(id, sharePost, "testUser");

                return "redirect:/share/" + id;
            }


            // ✅ 게시글 삭제 (본인만 가능)
            @PostMapping("/{id}/delete")
            public String deletePost(@PathVariable Long id) {
                sharePostService.deletePost(id, "testUser");
                return "redirect:/share/list";
            }

            // ✅ 게시글 검색
            @GetMapping("/search")
            public String searchPosts(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                      Model model) {
                List<SharePost> posts = sharePostService.searchPosts(keyword);
                model.addAttribute("posts", posts);
                return "share/sharePostList";
            }

        }
