package com.roomerang.contoller;

import com.roomerang.dto.UserDTO;
import com.roomerang.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/myInfo")
    public void showMyInfo(Long id, Model model){
        log.info("id = " + id);

        UserDTO userDTO = userService.showUserInfo(id);

        model.addAttribute("dto", userDTO);
    }

    @PostMapping("/remove")
    public ResponseEntity<String> remove(@RequestParam Long id, HttpServletRequest request, HttpServletResponse response) {
        // 1. 먼저 회원을 삭제
        boolean isDeleted = userService.remove(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원 탈퇴 실패");
        }

        // 2. 세션 무효화 (자동 로그아웃)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 3. 쿠키 삭제 (JWT 등 사용 시)
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("회원 탈퇴 완료");
    }
}

