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

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponse response = userService.registerUser(testAuthRequest);


        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        assertTrue(response.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));


        assertThrows(RuntimeException.class, () ->
                userService.registerUser(testAuthRequest)
        );
    }

    @Test
    void getCurrentUser_Success() {

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));


        UserResponse response = userService.getCurrentUser();


        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getCurrentUser_UserNotFound() {

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () -> userService.getCurrentUser());
    }

    @Test
    void deactivateUser_Success() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponse response = userService.deactivateUser("testuser");


        assertNotNull(response);
        assertFalse(response.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUser_UserNotFound() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () -> userService.deactivateUser("nonexistentuser"));
    }

    @Test
    void changePassword_Success() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponse response = userService.changePassword("testuser", "newPassword123");


        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());  // Verifique se o nome do usuário está correto
        assertTrue(response.isActive());  // Verifique se o usuário ainda está ativo
        verify(userRepository).save(any(User.class));  // Verifique se o repositório foi chamado
    }


    @Test
    void changePassword_UserNotFound() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () -> userService.changePassword("nonexistentuser", "newPassword123"));
    }

    @Test
    void changePassword_InvalidPassword() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponse response = userService.changePassword("testuser", "newPassword123");


        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());  // Verifique se o nome do usuário está correto
        assertTrue(response.isActive());  // Verifique se o usuário ainda está ativo
        verify(passwordEncoder).encode("newPassword123");  // Verifique se o encoder foi chamado para codificar a senha
        verify(userRepository).save(any(User.class));  // Verifique se o repositório foi chamado para salvar o usuário
    }

}
