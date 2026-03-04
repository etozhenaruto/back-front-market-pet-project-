package com.beertestshop.config;

import com.beertestshop.service.InMemoryUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Конфигурация Spring Security.
 * HttpSession (Cookie-based), CSRF включен для форм.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InMemoryUserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Сервис для загрузки пользователя по имени.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userService.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .disabled(!user.getIsActive())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Менеджер аутентификации.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    /**
     * Репозиторий контекста безопасности (HTTP Session).
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Конфигурация цепочки фильтров безопасности.
     * - PUBLIC: Swagger, статика, GET /api/products, POST /api/feedback
     * - USER: Корзина (/api/cart/*)
     * - ADMIN: Управление товарами и отзывами (/api/admin/*)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF защита (отключаем для API, используем для форм)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/**"
                        )
                )
                // Session management (Cookie-based)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                // Настройка доступа к эндпоинтам
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC: Статические ресурсы (Frontend)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/catalog.html",
                                "/cart.html",
                                "/feedback.html",
                                "/admin.html",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        // PUBLIC: Swagger и документация
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        // PUBLIC: GET продукты, POST отзывы, аутентификация
                        .requestMatchers("/api/v1/products/**").permitAll()
                        .requestMatchers("/api/v1/feedback/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // USER: Корзина
                        .requestMatchers("/api/v1/cart/**").hasAnyRole("USER", "ADMIN")
                        // ADMIN: Управление товарами и отзывами
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Форм логин (для браузера)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"username\":\"" + authentication.getName() +
                                    "\",\"message\":\"Login successful\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"Invalid credentials\"}");
                        })
                        .permitAll()
                )
                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                        .invalidateHttpSession(true)
                        .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID"))
                        .permitAll()
                )
                // Обработка ошибок аутентификации для API
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}
