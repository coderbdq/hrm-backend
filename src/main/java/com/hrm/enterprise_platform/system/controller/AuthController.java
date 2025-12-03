package com.hrm.enterprise_platform.system.controller;

import com.google.gson.Gson;
import com.hrm.enterprise_platform.config.JwtService;
import com.hrm.enterprise_platform.system.dto.AuthMeResponse;
import com.hrm.enterprise_platform.system.dto.FaceLoginRequest;
import com.hrm.enterprise_platform.system.dto.LoginRequest;
import com.hrm.enterprise_platform.system.entity.Role;
import com.hrm.enterprise_platform.system.entity.User;
import com.hrm.enterprise_platform.system.repository.UserRepository;
import com.hrm.enterprise_platform.system.service.AuthService;
import com.hrm.enterprise_platform.system.service.CustomUserDetailsService;
import com.hrm.enterprise_platform.system.util.CosineSimilarity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;   // ✅ THÊM

    // =========================
    // ĐĂNG NHẬP USER/PASS
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = authService.login(req.getUsername(), req.getPassword());

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(
                    new LoginResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            roleNames,
                            token
                    )
            );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    // Response chung cho /login và /face-login
    private record LoginResponse(
            Long id,
            String username,
            String fullName,
            Set<String> roles,
            String accessToken
    ) {
    }

    // =========================
    // /me – LẤY THÔNG TIN HIỆN TẠI
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            User user = authService.getCurrentUser(username);

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(
                    new AuthMeResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            roleNames
                    )
            );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token không hợp lệ");
        }
    }

    // =========================
    // /face-login – ĐĂNG NHẬP BẰNG FACEID
    // =========================
    @PostMapping("/face-login")
    public ResponseEntity<?> faceLogin(@RequestBody FaceLoginRequest req) {
        try {
            // ⚠ Demo: luôn login user "admin"
            User user = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (user.getFaceVector() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("User chưa đăng ký FaceID");
            }

            // Parse JSON từ DB -> double[]
            double[] dbVector = new Gson()
                    .fromJson(user.getFaceVector(), double[].class);

            double similarity = CosineSimilarity.similarity(
                    req.getVector(), dbVector);

            if (similarity < 0.85) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("FaceID không khớp (score=" + similarity + ")");
            }

            // Nếu khớp -> tạo token + trả JSON giống /login
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(
                    new LoginResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getFullName(),
                            roleNames,     // ✅ đúng kiểu Set<String>
                            token          // ✅ accessToken
                    )
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
