package com.mangakousei.mangakousei_backend.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET =
            "test-secret-key-for-ci-must-be-at-least-256-bits-long-padding-hehe";
    private static final int EXPIRATION_MS = 900000;
    private static final int REMEMBER_ME_MS = 604800000;
    private static final int REFRESH_MS    = 86400000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",       TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "rememberMeExpirationMs", REMEMBER_ME_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationMs",    REFRESH_MS);
    }

    private Authentication buildAuth(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, email, "hashed-pw", "Test User", null, authorities
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @Test
    @DisplayName("generateAccessToken: tạo token hợp lệ")
    void generateAccessToken_shouldReturnValidToken() {
        Authentication auth = buildAuth("user@test.com", "MANGAKA");

        String token = jwtTokenProvider.generateAccessToken(auth);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("getEmailFromToken: lấy đúng email từ token")
    void getEmailFromToken_shouldReturnCorrectEmail() {
        String email = "mangaka@test.com";
        Authentication auth = buildAuth(email, "MANGAKA");

        String token = jwtTokenProvider.generateAccessToken(auth);
        String extracted = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extracted).isEqualTo(email);
    }

    @Test
    @DisplayName("generateAccessToken: claims chứa roles đúng")
    void generateAccessToken_shouldContainRoles() {
        Authentication auth = buildAuth("admin@test.com", "ADMIN");

        String token = jwtTokenProvider.generateAccessToken(auth);
        Claims claims = jwtTokenProvider.extractAllClaims(token);

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertThat(roles).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("generateRefreshToken: tạo token hợp lệ (không rememberMe)")
    void generateRefreshToken_shouldReturnValidToken() {
        Authentication auth = buildAuth("user@test.com", "MANGAKA");

        String refreshToken = jwtTokenProvider.generateRefreshToken(auth, false);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("generateRefreshToken: claims có token_type = refresh")
    void generateRefreshToken_shouldHaveRefreshTokenType() {
        Authentication auth = buildAuth("user@test.com", "MANGAKA");

        String refreshToken = jwtTokenProvider.generateRefreshToken(auth, false);
        Claims claims = jwtTokenProvider.extractAllClaims(refreshToken);

        assertThat(claims.get("token_type")).isEqualTo("refresh");
    }

    @Test
    @DisplayName("validateToken: token giả mạo trả về false")
    void validateToken_withFakeToken_shouldReturnFalse() {
        boolean result = jwtTokenProvider.validateToken("this.is.fake");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateToken: token rỗng trả về false")
    void validateToken_withEmptyToken_shouldReturnFalse() {
        boolean result = jwtTokenProvider.validateToken("");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validateToken: token đúng trả về true")
    void validateToken_withValidToken_shouldReturnTrue() {
        Authentication auth = buildAuth("user@test.com", "MANGAKA");
        String token = jwtTokenProvider.generateAccessToken(auth);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("generateTokenFromEmail: tạo và validate thành công")
    void generateTokenFromEmail_shouldWork() {
        String token = jwtTokenProvider.generateTokenFromEmail(
                "direct@test.com", List.of("TANTOU"), false
        );

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("direct@test.com");
    }

    @Test
    @DisplayName("getExpirationRemaining: còn thời gian dương")
    void getExpirationRemaining_shouldBePositive() {
        Authentication auth = buildAuth("user@test.com", "MANGAKA");
        String token = jwtTokenProvider.generateAccessToken(auth);

        long remaining = jwtTokenProvider.getExpirationRemaining(token);
        assertThat(remaining).isPositive();
    }
}