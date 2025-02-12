package com.roomerang.contoller;

import com.roomerang.entity.Post;
import com.roomerang.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/board")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping("/")
    public String defaultPageRedirect() {
        return "redirect:/board/rooms";
    }

    //방있음
    @GetMapping("/rooms")
    public String listRooms(@RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postService.getPostsByCategory("방 있음", pageable);

        model.addAttribute("postsPage", postsPage);
        return "match/roomList";
    }

    //방없음
    @GetMapping("/no-rooms")
    public String listNoRooms(@RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postService.getPostsByCategory("방 없음", pageable);

        model.addAttribute("postsPage", postsPage);
        return "match/noRoomList";
    }

    //글 작성 페이지 이동
    @GetMapping("/post/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "match/postWrite";
    }

    //글 저장
    @PostMapping("/post/save")
    public String savePost(@RequestParam("rm_board_title") String rmBoardTitle,
                           @RequestParam("post_content") String postContent,
                           @RequestParam("amount") Integer amount,
                           @RequestParam("deposit") Integer deposit,
                           @RequestParam("author_region") String authorRegion,
                           @RequestParam("category") String category,
                           @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {

        Post post = new Post();
        post.setRm_board_title(rmBoardTitle);
        post.setPost_content(postContent);
        post.setAmount(amount != null ? amount : 0);
        post.setDeposit(deposit != null ? deposit : 0);
        post.setAuthor_region(authorRegion);
        post.setCategory(category);
        post.setPostDate(LocalDateTime.now());
        post.setPost_views(0);

        // 로그인 기능이 없으므로 임시 값
        post.setAuthor_age(25);
        post.setAuthor_name("테스트 사용자");
        post.setAuthor_gender("M");
        post.setUser_id("testUser"); // 임시 ID
        post.setUser_preference("default");

        // 파일 업로드 처리
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                String uploadDir = "C:/uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String originalFilename = photoFile.getOriginalFilename();
                String filePath = uploadDir + originalFilename;
                photoFile.transferTo(new File(filePath));

                post.setPhotoUrl("/uploads/" + originalFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 데이터베이스에 저장
        postService.savePost(post);

        return category.equals("방 있음") ? "redirect:/board/rooms" : "redirect:/board/no-rooms";
    }

    //글 조회 (방 있음 || 방 없음)
    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return "redirect:/board/rooms";
        }

        // 조회수 증가
        post.setPost_views(post.getPost_views() + 1);
        postService.savePost(post);

        model.addAttribute("post", post);

        return "방 있음".equals(post.getCategory()) ? "match/roomView" : "match/noRoomView";
    }

    //글 수정 페이지 이동 (본인이 작성한 글만 수정 가능)
    @GetMapping("/post/edit/{id}")
    public String editPost(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null || !"testUser".equals(post.getUser_id())) {
            return "redirect:/board/rooms"; // 본인 글이 아니면 목록으로 리다이렉트
        }

        model.addAttribute("post", post);
        return "match/postModify";
    }

    //글 수정 요청 처리
    @PostMapping("/post/update")
    public String updatePost(@RequestParam("id") Long id,
                             @RequestParam("rm_board_title") String rmBoardTitle,
                             @RequestParam("post_content") String postContent,
                             @RequestParam("author_region") String authorRegion,
                             @RequestParam("category") String category,
                             @RequestParam(value = "amount", required = false) Integer amount,
                             @RequestParam(value = "deposit", required = false) Integer deposit,
                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {

        Post post = postService.getPostById(id);
        if (post == null || !"testUser".equals(post.getUser_id())) {
            return "redirect:/board/rooms";
        }

        post.setRm_board_title(rmBoardTitle);
        post.setPost_content(postContent);
        post.setAuthor_region(authorRegion);
        post.setCategory(category);

        //방 있음일 경우만 금액, 보증금 처리
        if ("방 있음".equals(category)) {
            post.setAmount(amount != null ? amount : 0);
            post.setDeposit(deposit != null ? deposit : 0);
        }

        //이미지 파일 업로드 안됨 수정 필요
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                String uploadDir = "C:/uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String originalFilename = photoFile.getOriginalFilename();
                String filePath = uploadDir + originalFilename;
                photoFile.transferTo(new File(filePath));

                post.setPhotoUrl("/uploads/" + originalFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //수정된 내용 저장
        postService.savePost(post);

        return "redirect:/board/post/" + id;
    }
    // 글 삭제 (임시 사용자 "testUser"만 삭제 가능)
    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        Post post = postService.getPostById(id);
        if (post != null && "testUser".equals(post.getUser_id())) {
            postService.deletePost(id);
        }
        // 카테고리에 따라 목록 페이지로 이동
        return (post != null && "방 있음".equals(post.getCategory())) ? "redirect:/board/rooms" : "redirect:/board/no-rooms";
    }
}

