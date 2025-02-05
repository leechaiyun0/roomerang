package com.roomerang.contoller;

import com.roomerang.auth.JwtTokenProvider;
import com.roomerang.dto.request.*;
import com.roomerang.dto.response.UserFindResponse;
import com.roomerang.entity.User;
import com.roomerang.repository.UserRepository;
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

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // [회원가입] - 단계 0: 회원가입 페이지 접속
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new UserCreateRequest());
        return "auth/signup";
    }

    // [회원가입] - 단계 1: 사용자 정보 제출 및 검증 후 회원가입 처리
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
    public String login(@Validated @ModelAttribute ("loginForm") UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletRequest request) {
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

    // [아이디 찾기] - 단계 0: 비밀번호 재설정 페이지 접속
    @GetMapping("/find-id")
    public String findIdForm(Model model) {
        model.addAttribute("findIdForm", new UserFindRequest());
        return "auth/forgot-id-step1";
    }

    // [아이디 찾기] - 단계 1: 사용자 정보 제출 후, 마스킹된 아이디 목록과 보안 질문 조회
    @PostMapping("/find-id")
    public String findId(@Validated @ModelAttribute("findIdForm") UserFindRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            log.info("[find-id] 입력 에러: {}", bindingResult);
            return "auth/forgot-id-step1";
        }

        List<UserFindResponse> responses = userService.findMaskedUsersAndSecurityQuestion(request);

        if (responses.isEmpty()) {
            bindingResult.reject("error", "일치하는 사용자를 찾을 수 없습니다.");
            return "auth/forgot-id-step1";
        }

        model.addAttribute("userFindResponses", responses);
        return "auth/forgot-id-step2"; // 마스킹된 아이디 목록을 보여주는 페이지
    }

    // [아이디 찾기] - 단계 2: 보안 질문을 보여주는 폼
    @GetMapping("/find-id/security-question")
    public String showFindIdForm(@RequestParam("userId") Long userId, Model model) {
        String securityQuestion = userRepository.findSecurityQuestionByUserId(userId);
        model.addAttribute("securityQuestion", securityQuestion);
        model.addAttribute("userId", userId);
        model.addAttribute("verifyForm", new UserVerifyRequest());
        return "auth/forgot-id-step3";
    }

    // [아이디 찾기] - 단계 3: 보안 답변 검증 후 전체 아이디 반환
    @PostMapping("/find-id/verify")
    public String verifyIdSecurity(@Validated @ModelAttribute("verifyForm") UserVerifyRequest request,
                                   BindingResult bindingResult,
                                   Model model) {
        boolean isValid = userService.verifySecurityAnswerById(request.getUserId(), request.getSecurityAnswer());
        if (!isValid) {
            bindingResult.reject("error", "보안 질문 답변이 일치하지 않습니다.");
            return "auth/forgot-id-step3";
        }

        String fullUsername = userService.revealUsername(request.getUserId());
        model.addAttribute("username", fullUsername);
        return "auth/forgot-id-step4"; // 최종적으로 전체 아이디(실제 아이디)를 보여주는 페이지
    }

    // [비밀번호 찾기] - 단계 0: 비밀번호 재설정 페이지 접속
    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model) {
        model.addAttribute("resetPwForm", new UserFindRequest());
        return "auth/reset-password-step1";
    }

    // [비밀번호 찾기] - 단계 1: 사용자 정보 제출 후 보안 질문 반환
    @PostMapping("/reset-password/request")
    public String requestPasswordReset(
            @Validated @ModelAttribute("resetPwForm") UserFindRequest request,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /*bindingResult = 오류를 담는 객체
            만약 오류가 나면 다시 원 페이지로 돌아감
        */
        if (bindingResult.hasErrors()) {
            log.info("[reset-password] errors={}", bindingResult);
            return "auth/reset-password-step1";
        }

        UserFindResponse response = userService.validateUserForPasswordReset(request);

        if (response == null) {
            bindingResult.reject("error", "해당 정보로 가입된 사용자를 찾을 수 없습니다.");
            return "auth/reset-password-step1";
        }

        redirectAttributes.addAttribute("username", request.getUsername());
        redirectAttributes.addAttribute("securityQuestion", response.getSecurityQuestion());
        return "redirect:/auth/reset-password/security-question";
    }

    // [비밀번호 찾기] - 단계 2: 보안 질문을 보여주는 폼
    @GetMapping("/reset-password/security-question")
    public String showResetPasswordForm(@RequestParam("username") String username,
                                        @RequestParam("securityQuestion") String securityQuestion,
                                        Model model) {
        model.addAttribute("username", username);
        model.addAttribute("securityQuestion", securityQuestion);
        model.addAttribute("verifyForm", new UserVerifyRequest());
        return "auth/reset-password-step2";
    }


    // [비밀번호 재설정] - 단계 2: 보안 답변 검증 후 비밀번호 재설정 페이지로 리다이렉트
    @PostMapping("/reset-password/verify")
    public String verifyPasswordReset(
            @Validated @ModelAttribute("verifyForm") UserVerifyRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("[reset-password-verify] errors={}", bindingResult);
            return "auth/reset-password-step2";
        }

        boolean isValid = userService.verifySecurityAnswerByUsername(request.getUsername(), request.getSecurityAnswer());

        if (!isValid) {
            bindingResult.reject("error", "보안 질문 답변이 일치하지 않습니다.");
            return "auth/reset-password-step2";
        }

        String username = request.getUsername();
        String resetToken = jwtTokenProvider.createToken(username, List.of());

        return "redirect:/auth/reset-password/form?token=" + resetToken;
    }

    @GetMapping("/reset-password/form")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            model.addAttribute("error", "유효하지 않은 접근입니다.");
            return "auth/reset-password-step2";
        }

        model.addAttribute("token", token);
        model.addAttribute("passwordForm", new PasswordResetRequest());
        return "auth/reset-password-step3";
    }


    // [비밀번호 재설정 처리] - POST 요청: 폼 제출 시 새 비밀번호 저장
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @Validated @ModelAttribute ("passwordForm") PasswordResetRequest request,
                                BindingResult bindingResult, Model model) {
        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        userService.validatePassword(request.getNewPassword(), request.getPasswordConfirm(), bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("[password-reset] errors={}", bindingResult);
            model.addAttribute("token", token);
            return "auth/reset-password-step3";
        }

        // JWT 토큰을 사용하여 사용자 정보를 확인
        String username = jwtTokenProvider.getUsername(token);

        if (username == null) {
            bindingResult.reject("verifyFail", "유효하지 않은 접근입니다.");
            model.addAttribute("token", token);
            return "auth/reset-password-step3";
        }

        // 사용자 확인 후 비밀번호 변경 로직 실행
        boolean success = userService.resetPassword(username, request.getNewPassword());

        if (!success) {
            bindingResult.reject("error", "비밀번호 변경에 실패했습니다.");
            model.addAttribute("token", token);
            return "auth/reset-password-step3"; // 실패 시 에러 메시지 출력
        }

        log.info("비밀번호 재설정 성공");
        return "auth/reset-password-success"; // 비밀번호 변경 성공 후 로그인 페이지로 리다이렉트
    }
}
