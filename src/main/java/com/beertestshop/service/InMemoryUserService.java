package com.beertestshop.service;

import com.beertestshop.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для управления пользователями с хранением данных в памяти.
 * Использует ConcurrentHashMap для потокобезопасности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryUserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    /**
     * Сохранить пользователя.
     */
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        log.debug("User saved: {}", user);
        return user;
    }

    /**
     * Найти пользователя по ID.
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Найти пользователя по имени.
     */
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Получить всех пользователей.
     */
    public Map<Long, User> findAll() {
        return Map.copyOf(users);
    }

    /**
     * Удалить пользователя по ID.
     */
    public boolean delete(Long id) {
        return users.remove(id) != null;
    }

    /**
     * Активировать/деактивировать пользователя.
     */
    public User setActive(Long id, boolean isActive) {
        return findById(id).map(user -> {
            user.setIsActive(isActive);
            return save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Проверить существование пользователя по имени.
     */
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    /**
     * Инициализировать тестовые данные.
     */
    public void initTestData() {
        log.info("Initializing users...");

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();
        save(admin);

        User user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .role(User.Role.USER)
                .isActive(true)
                .build();
        save(user);

        log.info("Users initialized: admin, user");
    }
}
