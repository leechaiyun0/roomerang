package com.roomerang.contoller;

import com.roomerang.dto.request.UserCreateRequest;
import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserVerifyRequest;
import com.roomerang.dto.response.UserFindResponse;
import com.roomerang.entity.User;
import com.roomerang.repository.UserRepository;
import com.roomerang.service.UserService;
import com.roomerang.util.Action;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.roomerang.util.StringHelper.generateTempPassword;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, String>> checkUsername(@RequestParam("username") String username) {
        Map<String, String> response = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            response.put("error", "아이디를 입력해 주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean exists = userRepository.existsByUsername(username);
        if (exists) {
            response.put("error", "이미 사용 중인 아이디입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "사용 가능한 아이디입니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/retrieve/sequrity-question")
    public ResponseEntity<Map<String, String>> viewSecurityQuestion(@RequestParam("userId") Long userId) {
        Map<String, String> response = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            response.put("error", "해당 정보로 가입된 사용자를 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", user.getSecurityQuestion());
        return ResponseEntity.ok(response);
    }

}
