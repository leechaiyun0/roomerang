package com.roomerang.contoller;

import com.roomerang.auth.JwtTokenProvider;
import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserVerifyRequest;
import com.roomerang.dto.response.UserFindResponse;
import com.roomerang.repository.UserRepository;
import com.roomerang.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, String>> checkUsername(@RequestParam("username") String username) {
        Map<String, String> response = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            response.put("error", "아이디를 입력해 주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        boolean exists = userRepository.existsByUsername(username);
        if (exists) {
            response.put("error", "이미 사용 중인 아이디입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        response.put("message", "사용 가능한 아이디입니다.");
        return ResponseEntity.ok(response);
    }

    // [아이디 찾기] - 단계 1: 사용자 정보로 마스킹된 아이디 목록과 해당 아이디의 보안 질문 조회
    @PostMapping("/find-id")
    public ResponseEntity<List<UserFindResponse>> findId(@RequestBody UserFindRequest request) {
        List<UserFindResponse> responses = userService.findMaskedUsersAndSecurityQuestion(request);
        if (responses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(responses);
    }

    // [아이디 찾기] - 단계 2: 보안 답변 검증 후 전체 아이디 반환
    @PostMapping("/find-id/verify")
    public ResponseEntity<?> verifyIdSecurity(@RequestBody UserVerifyRequest request) {
        boolean isValid = userService.verifySecurityAnswer(request);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("보안 질문 답변이 일치하지 않습니다.");
        }
        String fullUsername = userService.revealUsername(request.getUserId());
        return ResponseEntity.ok(Collections.singletonMap("username", fullUsername));
    }

    // [비밀번호 찾기] - 단계 1: 사용자 정보 제출 후 보안 질문 반환
    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody UserFindRequest request) {
        UserFindResponse response = userService.validateUserForPasswordReset(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("일치하는 사용자가 없습니다.");
        }
        String securityQuestion = response.getSecurityQuestion();
        return ResponseEntity.ok(Collections.singletonMap("securityQuestion", securityQuestion));
    }

}
