package com.roomerang.contoller;

import com.roomerang.entity.Post;
import com.roomerang.entity.User;
import com.roomerang.service.PostService;
import com.roomerang.util.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                            Model model,
                            HttpServletRequest request) {

        // 세션에서 로그인한 사용자 정보 가져오기
        // 세션에서 사용자 정보 가져오기 (필요 시 생성)
        HttpSession session = request.getSession(true);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

// 디버그 로그 추가
        if (loginUser == null) {
            System.out.println("🚨 [DEBUG] loginUser is NULL!");
        } else {
            System.out.println("✅ [DEBUG] loginUser: " + loginUser.getUsername());
        }

// 모델에 사용자 정보 추가
        model.addAttribute("loginUser", loginUser);

        // 로그인한 사용자 정보를 모델에 추가
        model.addAttribute("loginUser", loginUser);

        // 게시글 목록 가져오기
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postService.getPostsByCategory("방 있음", pageable);

        model.addAttribute("postsPage", postsPage);
        return "match/roomList";
    }

    //방없음
    @GetMapping("/no-rooms")
    public String listNoRooms(@RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size,
                              Model model,
                              HttpServletRequest request) {

        // 세션에서 로그인한 사용자 정보 가져오기
        // 세션에서 사용자 정보 가져오기 (필요 시 생성)
        HttpSession session = request.getSession(true);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

// 디버그 로그 추가
        if (loginUser == null) {
            System.out.println("🚨 [DEBUG] loginUser is NULL!");
        } else {
            System.out.println("✅ [DEBUG] loginUser: " + loginUser.getUsername());
        }

// 모델에 사용자 정보 추가
        model.addAttribute("loginUser", loginUser);

        // 로그인한 사용자 정보를 모델에 추가
        model.addAttribute("loginUser", loginUser);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postService.getPostsByCategory("방 없음", pageable);

        model.addAttribute("postsPage", postsPage);
        return "match/noRoomList";
    }

    //글 작성
    @GetMapping("/post/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "match/postWrite";
    }

    //글 저장
    @PostMapping("/post/save")
    public String savePost(@RequestParam("rmBoardTitle") String rmBoardTitle,
                           @RequestParam("postContent") String postContent,
                           @RequestParam("authorRegion") String authorRegion,
                           @RequestParam("category") String category,
                           @RequestParam(value = "amount", required = false) Integer amount,
                           @RequestParam(value = "deposit", required = false) Integer deposit,
                           HttpServletRequest request) {
        // 세션에서 로그인한 사용자 가져오기
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login"; // 로그인하지 않았다면 로그인 페이지로 이동
        }

        Post post = new Post();
        post.setRmBoardTitle(rmBoardTitle);
        post.setPostContent(postContent);
        post.setAuthorRegion(authorRegion);
        post.setCategory(category);
        post.setPostDate(LocalDateTime.now());
        post.setPostViews(0);

        // 로그인한 사용자 정보 저장
        post.setAuthorAge(25); // 나이 데이터 추가 필요
        post.setAuthorName(loginUser.getName()); // 로그인한 사용자의 이름
        post.setAuthorGender(loginUser.getGender().toString()); // 성별 (MALE, FEMALE)
        post.setUserId(loginUser.getUsername()); // 로그인한 사용자의 ID
        post.setUserPreference("default");

        // 방 있음인 경우만 금액과 보증금 설정
        if ("방 있음".equals(category)) {
            post.setAmount(amount != null ? amount : 0);
            post.setDeposit(deposit != null ? deposit : 0);
        } else {
            post.setAmount(0);
            post.setDeposit(0);
        }

        postService.savePost(post);
        return "redirect:/board/" + (category.equals("방 있음") ? "rooms" : "no-rooms");
    }

    //글 조회 (방 있음 || 방 없음)
    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return "redirect:/board/rooms";
        }

        // 조회수 증가
        post.setPostViews(post.getPostViews() + 1);
        postService.savePost(post);

        // 현재 로그인한 사용자 가져오기
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser != null) {
            System.out.println("로그인한 사용자 ID: " + loginUser.getUsername());
            System.out.println("게시글 작성자 ID: " + post.getUserId());
        } else {
            System.out.println("로그인한 사용자가 없음");
        }

        model.addAttribute("post", post);
        model.addAttribute("loginUser", loginUser); // 모델에 추가하여 HTML에서 비교 가능하도록 설정

        return "방 있음".equals(post.getCategory()) ? "match/roomView" : "match/noRoomView";
    }


    //글 수정
    @GetMapping("/post/edit/{id}")
    public String editPost(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login"; // 로그인하지 않았다면 로그인 페이지로 이동
        }

        Post post = postService.getPostById(id);
        if (post == null || !post.getUserId().equals(loginUser.getUsername())) {
            return "redirect:/board/rooms"; // 본인 글이 아니면 목록으로 리다이렉트
        }

        model.addAttribute("post", post);
        return "match/postModify";
    }


    // 글 수정 요청 처리
    @PostMapping("/post/update")
    public String updatePost(@RequestParam("id") Long id,
                             @RequestParam("rmBoardTitle") String rmBoardTitle,
                             @RequestParam("postContent") String postContent,
                             @RequestParam("authorRegion") String authorRegion,
                             @RequestParam("category") String category,
                             @RequestParam(value = "amount", required = false) Integer amount,
                             @RequestParam(value = "deposit", required = false) Integer deposit,
                             HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login"; // 로그인하지 않았다면 로그인 페이지로 이동
        }

        Post post = postService.getPostById(id);
        if (post == null || !post.getUserId().equals(loginUser.getUsername())) {
            return "redirect:/board/rooms";
        }

        post.setRmBoardTitle(rmBoardTitle);
        post.setPostContent(postContent);
        post.setAuthorRegion(authorRegion);
        post.setCategory(category);

        if ("방 있음".equals(category)) {
            post.setAmount(amount != null ? amount : 0);
            post.setDeposit(deposit != null ? deposit : 0);
        } else {
            post.setAmount(0);
            post.setDeposit(0);
        }

        postService.savePost(post);
        return "redirect:/board/post/" + id;
    }

    //글 삭제
    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login"; // 로그인하지 않았다면 로그인 페이지로 이동
        }

        Post post = postService.getPostById(id);
        if (post != null && post.getUserId().equals(loginUser.getUsername())) {
            postService.deletePost(id);
        }

        return (post != null && "방 있음".equals(post.getCategory())) ? "redirect:/board/rooms" : "redirect:/board/no-rooms";
    }


    //검색 기능
    @GetMapping("/search")
    public String searchPosts(@RequestParam(name = "category") String category,
                              @RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            postsPage = postService.getPostsByCategory(category, pageable);
        } else {
            postsPage = postService.searchPostsByCategory(category, keyword, pageable);
        }

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);


        return category.equals("방 있음") ? "match/roomList" : "match/noRoomList";
    }
}
