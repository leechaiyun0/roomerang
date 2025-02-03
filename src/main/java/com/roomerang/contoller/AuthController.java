package com.roomerang.contoller;

import com.roomerang.auth.JwtTokenProvider;
import com.roomerang.dto.request.UserCreateRequest;
import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserLoginRequest;
import com.roomerang.dto.request.UserVerifyRequest;
import com.roomerang.entity.User;
import com.roomerang.service.UserService;
import com.roomerang.util.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new UserCreateRequest());
        return "auth/signup";
    }

    @GetMapping("/find-id")
    public String findIdForm(Model model) {
        model.addAttribute("findIdForm", new UserFindRequest());
        return "retrieveid";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model) {
        model.addAttribute("resetPwForm", new UserFindRequest());
        return "auth/retrieve-password";
    }

    @PostMapping("/signup")
    public String signupUser(@Validated @ModelAttribute("signupForm") UserCreateRequest userCreateRequest,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        userService.validatePassword(userCreateRequest.getPassword(), userCreateRequest.getPasswordConfirm(), bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("[Signup] errors={}", bindingResult);
            return "auth/signup";
        }

        boolean signupSuccess = userService.createUser(userCreateRequest, bindingResult);

        if (!signupSuccess) {
            return "auth/signup";
        }

        log.info("회원가입 성공");
        redirectAttributes.addFlashAttribute("username", userCreateRequest.getUsername());
        redirectAttributes.addFlashAttribute("status", true);
        return "auth/success";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new UserLoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute  ("loginForm") UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.info("[Login] errors={}", bindingResult);
            return "auth/login";
        }

        User loginUser = userService.login(userLoginRequest.getUsername(), userLoginRequest.getPassword());

        if (loginUser == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "auth/login";
        }

        log.info("로그인 성공");
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_USER, loginUser);

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    // [비밀번호 재설정] - 단계 2: 보안 답변 검증 후 비밀번호 재설정 페이지로 리다이렉트
    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> verifyPasswordReset(@RequestBody UserVerifyRequest request,
                                                 HttpServletRequest httpRequest) {
        boolean isValid = userService.verifySecurityAnswer(request);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("보안 질문 답변이 일치하지 않습니다.");
        }

        // userService 혹은 request에서 사용자 식별자(예: userId)를 얻을 수 있다고 가정.
        // 여기서는 request.getUserId()가 Long 타입을 반환하므로 String으로 변환합니다.
        String userUid = String.valueOf(request.getUserId());

        // 비밀번호 재설정을 위한 JWT 토큰 생성 (역할 정보가 필요없으면 빈 리스트 전달)
        String resetToken = jwtTokenProvider.createToken(userUid, List.of());

        // 현재 요청 정보를 기반으로 절대 URL 생성
        URI redirectUri = ServletUriComponentsBuilder.fromRequestUri(httpRequest)
                .replacePath("/users/reset-password-form")
                .replaceQuery(null) // 기존 쿼리 제거
                .queryParam("token", resetToken)
                .build()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    // [비밀번호 재설정 폼] - GET 요청: 재설정 페이지 렌더링
    @GetMapping("/reset-password-form")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // 토큰을 모델에 포함하여 폼에 전달 (숨은 필드나 자바스크립트로 사용할 수 있음)
        model.addAttribute("token", token);
        return "auth/reset-password-form";
    }

    // [비밀번호 재설정 처리] - POST 요청: 폼 제출 시 새 비밀번호 저장
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("token", token);
            return "auth/reset-password-form"; // 비밀번호 재설정 폼 재렌더링
        }

        // JWT 토큰을 사용하여 사용자 정보를 확인
        String username = jwtTokenProvider.getUsername(token);
        if (username == null) {
            model.addAttribute("error", "유효하지 않은 토큰입니다.");
            model.addAttribute("token", token);
            return "auth/reset-password-form"; // 유효하지 않은 토큰
        }

        // 사용자 확인 후 비밀번호 변경 로직 실행
        boolean success = userService.resetPassword(username, newPassword);

        if (!success) {
            model.addAttribute("error", "비밀번호 변경에 실패했습니다.");
            model.addAttribute("token", token);
            return "auth/reset-password-form"; // 실패 시 에러 메시지 출력
        }

        return "redirect:/login"; // 비밀번호 변경 성공 후 로그인 페이지로 리다이렉트
    }
}
