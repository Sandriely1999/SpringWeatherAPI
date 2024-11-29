package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.requests.AuthRequest;
import org.educandoweb.springweatherdata.responses.AuthResponse;
import org.educandoweb.springweatherdata.responses.UserResponse;
import org.educandoweb.springweatherdata.service.UserService;
import org.educandoweb.springweatherdata.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationController authController;

    private AuthRequest authRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        userResponse = UserResponse.builder()
                .username("testuser")
                .active(true)
                .build();
    }

    @Test
    void register_Success() {
        // Arrange
        when(userService.registerUser(any(AuthRequest.class)))
                .thenReturn(userResponse);
        when(jwtUtil.generateToken(anyString()))
                .thenReturn("test.jwt.token");

        // Act
        ResponseEntity<AuthResponse> response =
                authController.register(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("testuser", response.getBody().getUsername());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(jwtUtil.generateToken(anyString()))
                .thenReturn("test.jwt.token");

        // Act
        ResponseEntity<AuthResponse> response =
                authController.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test.jwt.token", response.getBody().getToken());
    }
}