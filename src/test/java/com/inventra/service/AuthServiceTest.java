package com.inventra.service;

import com.inventra.dto.request.LoginRequest;
import com.inventra.dto.request.RegisterRequest;
import com.inventra.dto.response.AuthResponse;
import com.inventra.entity.Role;
import com.inventra.entity.User;
import com.inventra.exception.BadRequestException;
import com.inventra.repository.UserRepository;
import com.inventra.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest(String email) {
        RegisterRequest r = new RegisterRequest();
        r.setName("Test User");
        r.setEmail(email);
        r.setPassword("password123");
        return r;
    }

    private User user(Long id, String email, Role role, String encodedPassword) {
        return User.builder()
                .id(id)
                .name("Test User")
                .email(email)
                .password(encodedPassword)
                .role(role)
                .build();
    }

    @Test
    void register_shouldAssignAdminRole_toFirstUser() {
        RegisterRequest request = registerRequest("admin@test.com");
        User savedUser = user(1L, "admin@test.com", Role.ADMIN, "encoded");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("admin@test.com", Role.ADMIN)).thenReturn("token-admin");

        AuthResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getToken()).isEqualTo("token-admin");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        verify(userRepository).save(argThat(u -> u.getRole() == Role.ADMIN));
    }

    @Test
    void register_shouldAssignStaffRole_toSubsequentUsers() {
        RegisterRequest request = registerRequest("staff@test.com");
        User savedUser = user(2L, "staff@test.com", Role.STAFF, "encoded");

        when(userRepository.existsByEmail("staff@test.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("staff@test.com", Role.STAFF)).thenReturn("token-staff");

        AuthResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.STAFF);
        assertThat(response.getToken()).isEqualTo("token-staff");
        verify(userRepository).save(argThat(u -> u.getRole() == Role.STAFF));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = registerRequest("existing@test.com");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldSucceed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        User user = user(1L, "user@test.com", Role.STAFF, "encoded");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user@test.com", Role.STAFF)).thenReturn("token-staff");

        AuthResponse response = authService.login(request);

        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getRole()).isEqualTo(Role.STAFF);
        assertThat(response.getToken()).isEqualTo("token-staff");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user@test.com", "password123"));
    }

    @Test
    void login_shouldThrow_whenBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or password");
    }
}
