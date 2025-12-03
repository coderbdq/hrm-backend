package com.hrm.enterprise_platform.system.service;

import com.hrm.enterprise_platform.system.dto.AuthMeResponse;
import com.hrm.enterprise_platform.system.dto.LoginRequest;
import com.hrm.enterprise_platform.system.entity.User;
import com.hrm.enterprise_platform.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User login(String username, String rawPassword) {

        // Tìm user theo username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // Kiểm tra password bằng BCrypt
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return user;
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    public AuthMeResponse getCurrentUserInfo(String username) {
        User user = getCurrentUser(username);

        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        return new AuthMeResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                roleNames
        );
    }
}