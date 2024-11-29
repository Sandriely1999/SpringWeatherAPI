package org.educandoweb.springweatherdata.service;

import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.requests.AuthRequest;
import org.educandoweb.springweatherdata.responses.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private AuthRequest testAuthRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        testAuthRequest = new AuthRequest();
        testAuthRequest.setUsername("testuser");
        testAuthRequest.setPassword("password123");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.registerUser(testAuthRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        assertTrue(response.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                userService.registerUser(testAuthRequest)
        );
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = userService.getCurrentUser();

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void deactivateUser_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.deactivateUser("testuser");

        // Assert
        assertNotNull(response);
        assertFalse(response.isActive());
    }
}