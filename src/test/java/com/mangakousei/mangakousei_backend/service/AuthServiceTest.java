package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.RegisterReq;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.Role;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import com.mangakousei.mangakousei_backend.security.CustomUserDetails;
import com.mangakousei.mangakousei_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CookieService cookieService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        Role mangakaRole = new Role(1L, "MANGAKA");
        mockUser = User.builder()
                .userId(1L)
                .fullName("Test User")
                .email("test@test.com")
                .passwordHash("hashed-pw")
                .roles(List.of(mangakaRole))
                .build();
    }

    @Test
    @DisplayName("register: thành công với email mới")
    void register_withNewEmail_shouldReturnUserInfo() {
        RegisterReq req = new RegisterReq();
        req.setFullName("Test User");
        req.setEmail("new@test.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserInfoRes result = authService.register(req);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getFullName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: email đã tồn tại → ném CustomAppException 409")
    void register_withExistingEmail_shouldThrowConflict() {
        RegisterReq req = new RegisterReq();
        req.setFullName("Test User");
        req.setEmail("existing@test.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(CustomAppException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: password được encode trước khi lưu")
    void register_shouldEncodePassword() {
        RegisterReq req = new RegisterReq();
        req.setFullName("Test User");
        req.setEmail("new2@test.com");
        req.setPassword("plaintext");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        authService.register(req);

        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    @DisplayName("getUserInfo: có authentication hợp lệ → trả về info đúng")
    void getUserInfo_withValidAuth_shouldReturnUserInfo() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "test@test.com", "hashed-pw", "Test User", null,
                List.of(new SimpleGrantedAuthority("MANGAKA"))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserInfoRes result = authService.getUserInfo();

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getRoles()).containsExactly("MANGAKA");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUserInfo: không có authentication → ném Unauthorized")
    void getUserInfo_withNoAuth_shouldThrowUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.getUserInfo())
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> {
                    CustomAppException cae = (CustomAppException) ex;
                    assertThat(cae.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }
}