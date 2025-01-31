package com.roomerang.service;

import com.roomerang.dto.request.UserCreateRequest;
import com.roomerang.dto.request.UserFindRequest;
import com.roomerang.dto.request.UserVerifyRequest;
import com.roomerang.dto.response.UserFindResponse;
import com.roomerang.entity.User;
import com.roomerang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

import static com.roomerang.util.StringHelper.maskUsername;
import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void validatePassword(String password, String confirmPassword, BindingResult bindingResult) {
        if (!password.equals(confirmPassword)) {
            bindingResult.rejectValue("passwordConfirm", "passwordConfirmError", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    @Override
    public boolean createUser(UserCreateRequest userCreateRequest, BindingResult bindingResult) {
        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "이미 사용 중인 아이디입니다.");
            return false;
        }

        User user = new User(
                userCreateRequest.getName(),
                userCreateRequest.getBirthDate(),
                userCreateRequest.getGender(),
                userCreateRequest.getUsername(),
                userCreateRequest.getSecurityQuestion(),
                userCreateRequest.getSecurityAnswer(),
                passwordEncoder.encode(userCreateRequest.getPassword()));
        userRepository.save(user);
        return true;
    }

    @Override
    public User login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(null);
    }

    @Override
    public List<UserFindResponse> getMaskedUsers(UserFindRequest userFindRequest) {
        List<User> foundUsers = userRepository.findAllByNameAndBirthDateAndGender(
                userFindRequest.getName(),
                userFindRequest.getBirthDate(),
                userFindRequest.getGender());

        if (foundUsers.isEmpty()) {
            return emptyList();
        }

        return foundUsers.stream()
                .map(user -> new UserFindResponse(
                        maskUsername(user.getUsername()),
                        user.getId()))
                .toList();
    }

    @Override
    public boolean verifySequrityAnswer(UserVerifyRequest verifyRequest) {
        User savedUser = userRepository.findById(verifyRequest.getUserId())
                .orElse(null);

        if (savedUser == null) {
            return false;
        }

        return savedUser.getSecurityAnswer().equals(verifyRequest.getSecurityAnswer());
    }

    @Override
    public String revealUsername(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return "";
        }

        return user.getUsername();
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

