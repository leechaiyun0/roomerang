package com.roomerang.contoller;

import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserLoginRequest;
import com.roomerang.dto.request.UserCreateRequest;
import com.roomerang.dto.request.UserVerifyRequest;
import com.roomerang.dto.response.UserFindResponse;
import com.roomerang.entity.User;
import com.roomerang.repository.UserRepository;
import com.roomerang.service.UserService;
import com.roomerang.util.Action;
import com.roomerang.util.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.roomerang.util.StringHelper.generateTempPassword;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new UserCreateRequest());
        return "users/signup";
    }

    @GetMapping("/retrieve/id")
    public String idFindForm(Model model) {
        model.addAttribute("findForm", new UserFindRequest());
        return "users/retrieveid";
    }

    @PostMapping("/retrieve/id")
    public String retrieveId(
            @Validated @ModelAttribute("findForm") UserFindRequest userFindRequest,
            BindingResult bindingResult,
            Model model) {

        List<UserFindResponse> userFindResponses = userService.getMaskedUsers(userFindRequest);

        if (userFindResponses.isEmpty()) {
            bindingResult.reject("idFindFail", "일치하는 사용자를 찾을 수 없습니다.");
            return "users/retrieveid";
        }

        model.addAttribute("userFindResponses", userFindResponses);

        return "users/retrieveidresult";
    }

    @GetMapping("/retrieve/password")
    public String pwFindForm(Model model) {
        model.addAttribute("passwdFindForm", new UserFindRequest());
        return "users/retrievepassword";
    }

    @PostMapping("/retrieve/security-answer")
    public String verifySecurityAnswer(
            @ModelAttribute("userVerifyRequest") UserVerifyRequest userVerifyRequest,
            BindingResult bindingResult,
            Model model) {
        boolean validated = userService.verifySequrityAnswer(userVerifyRequest);

        if (!validated) {
            bindingResult.reject("securityAnswerInvalid", "보안 질문 답변이 일치하지 않습니다.");
            return "users/retrieveid";
        }

        log.info("보안 질문 답변 검증 성공");
        Action action = (Action) model.getAttribute("action");
        if (action == null) {
            bindingResult.reject("actionTypeError", "진행 중인 요청이 만료되었습니다. 처음부터 다시 시도해 주세요.");
            return "users/retrieveid";
        }

        switch (action) {
            case FIND_ID:
                String foundUsername = userService.revealUsername(userVerifyRequest.getUserId());
                model.addAttribute("username", foundUsername);  // 아이디를 모델에 추가
                return "users/findresult";

            case RESET_PASSWORD:
                String tempPassword = generateTempPassword(8);
                userService.resetPassword(userVerifyRequest.getUserId(), tempPassword);
                model.addAttribute("tempPassword", tempPassword);
                return "users/findresult";

            default:
                bindingResult.reject("actionTypeError", "알 수 없는 actionType");
                return "users/retrieveid";
        }
    }

    @PostMapping("/signup")
    public String signupUser(@Validated @ModelAttribute("signupForm") UserCreateRequest userCreateRequest,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        userService.validatePassword(userCreateRequest.getPassword(), userCreateRequest.getPasswordConfirm(), bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("[Signup] errors={}", bindingResult);
            return "users/signup";
        }

        boolean signupSuccess = userService.createUser(userCreateRequest, bindingResult);

        if (!signupSuccess) {
            return "users/signup";
        }

        log.info("회원가입 성공");
        redirectAttributes.addFlashAttribute("username", userCreateRequest.getUsername());
        redirectAttributes.addFlashAttribute("status", true);
        return "users/success";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new UserLoginRequest());
        return "users/login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute  ("loginForm") UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.info("[Login] errors={}", bindingResult);
            return "users/login";
        }

        User loginUser = userService.login(userLoginRequest.getUsername(), userLoginRequest.getPassword());

        if (loginUser == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "users/login";
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


}
