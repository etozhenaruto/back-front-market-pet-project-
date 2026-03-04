package com.beertestshop.controller;

import com.beertestshop.dto.LoginRequest;
import com.beertestshop.dto.LoginResponse;
import com.beertestshop.model.User;
import com.beertestshop.service.InMemoryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST контроллер для аутентификации.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации пользователей")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final InMemoryUserService userService;

    /**
     * Аутентификация пользователя (login).
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя по имени и паролю")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            // Создаем токен аутентификации
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            // Аутентифицируем
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Сохраняем в SecurityContext
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Получаем информацию о пользователе
            Optional<User> userOpt = userService.findByUsername(request.getUsername());
            String role = userOpt.map(u -> u.getRole().name()).orElse("USER");

            log.info("Login successful for user: {}", request.getUsername());

            LoginResponse response = LoginResponse.builder()
                    .username(request.getUsername())
                    .role(role)
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: Invalid credentials", request.getUsername());
            return ResponseEntity.status(401).body(
                    LoginResponse.builder()
                            .username(request.getUsername())
                            .message("Invalid username or password")
                            .build()
            );
        }
    }

    /**
     * Выход из системы (logout).
     */
    @PostMapping("/logout")
    @Operation(summary = "Выход из системы", description = "Завершает сессию пользователя")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        log.info("Logout request for user: {}", username);

        // Очищаем SecurityContext
        SecurityContextHolder.clearContext();

        // Аннулируем сессию
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        log.info("Logout successful for user: {}", username);

        LoginResponse logoutResponse = LoginResponse.builder()
                .username(username)
                .message("Logout successful")
                .build();

        return ResponseEntity.ok(logoutResponse);
    }

    /**
     * Получить информацию о текущем пользователе.
     */
    @PostMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Возвращает информацию о текущем авторизованном пользователе")
    public ResponseEntity<LoginResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(
                    LoginResponse.builder()
                            .message("Not authenticated")
                            .build()
            );
        }

        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        String role = userOpt.map(u -> u.getRole().name()).orElse("USER");

        log.debug("Current user: {}, role: {}", username, role);

        LoginResponse response = LoginResponse.builder()
                .username(username)
                .role(role)
                .message("User info retrieved")
                .build();

        return ResponseEntity.ok(response);
    }
}
