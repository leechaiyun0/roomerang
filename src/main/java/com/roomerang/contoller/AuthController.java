package com.roomerang.contoller;

import com.roomerang.dto.request.UserCreateRequest;
import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserLoginRequest;
import com.roomerang.entity.User;
import com.roomerang.service.UserService;
import com.roomerang.util.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new UserCreateRequest());
        return "users/signup";
    }

    @GetMapping("/find-id")
    public String findIdForm(Model model) {
        model.addAttribute("findIdForm", new UserFindRequest());
        return "auth/retrieveid";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model) {
        model.addAttribute("resetPwForm", new UserFindRequest());
        return "auth/retrievepassword";
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
        return "users/success";
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
}
