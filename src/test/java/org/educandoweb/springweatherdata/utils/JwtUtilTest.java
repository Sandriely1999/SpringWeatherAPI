package org.educandoweb.springweatherdata.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long EXPIRATION = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY, EXPIRATION);
    }

    @Test
    void generateToken_ValidUsername_Success() {
        // Act
        String token = jwtUtil.generateToken("testuser");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUsername_ValidToken_Success() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_ValidToken_Success() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        boolean isValid = jwtUtil.validateToken(token, "testuser");

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WrongUsername_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act & Assert
        assertFalse(jwtUtil.validateToken(token, "wronguser"));
    }
}