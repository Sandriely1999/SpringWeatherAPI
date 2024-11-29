package org.educandoweb.springweatherdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.requests.AuthRequest;
import org.educandoweb.springweatherdata.responses.UserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.educandoweb.springweatherdata.repositories.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerUser(AuthRequest request) {
        log.info("Tentando registrar novo usuário: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Tentativa de registro com username já existente: {}", request.getUsername());
            throw new RuntimeException("Username já existe");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", request.getUsername());

        return convertToResponse(savedUser);
    }

    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return convertToResponse(user);
    }

    public UserResponse deactivateUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("Usuário desativado: {}", username);

        return convertToResponse(savedUser);
    }

    public UserResponse changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("Senha alterada para o usuário: {}", username);

        return convertToResponse(savedUser);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}