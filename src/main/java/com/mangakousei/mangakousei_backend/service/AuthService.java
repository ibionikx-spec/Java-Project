package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.LoginReq;
import com.mangakousei.mangakousei_backend.dto.request.RegisterReq;
import com.mangakousei.mangakousei_backend.dto.response.LoginRes;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.Role;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import com.mangakousei.mangakousei_backend.security.CustomUserDetails;
import com.mangakousei.mangakousei_backend.security.CustomUserDetailsService;
import com.mangakousei.mangakousei_backend.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;

    public LoginRes login(LoginReq request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomAppException("Unauthorized", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication, request.isRememberMe());

        // 7 ngay va 1 ngay
        long refreshMaxAge = request.isRememberMe() ? 7 * 24 * 60 * 60 : 24 * 60 * 60;

        cookieService.setAuthBothCookies(response, accessToken, refreshToken, refreshMaxAge);

        return LoginRes.builder()
                .id(userDetails.getId())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .avatarUrl(userDetails.getAvatarUrl())
                .message("Login successful")
                .build();
    }

    public LoginRes refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie: request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new CustomAppException("Please login to continue!", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            cookieService.clearAuthCookies(response, refreshToken);
            throw new CustomAppException("Login session has expired, please log in again!", HttpStatus.UNAUTHORIZED);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!(userDetails instanceof CustomUserDetails customUser)) {
            throw new CustomAppException("Failed handle user", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // 7 ngay va 1 ngay
        Claims claims = jwtTokenProvider.extractClaimsEvenIfExpired(refreshToken);
        boolean isRememberMe = claims.get("rememberMe") != null && (boolean) claims.get("rememberMe");
        long refreshMaxAge = isRememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60;

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication, isRememberMe);

        cookieService.setAuthBothCookies(response, newAccessToken, newRefreshToken, refreshMaxAge);

        return LoginRes.builder()
                .id(customUser.getId())
                .fullName(customUser.getFullName())
                .email(customUser.getEmail())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .message("Token refresh successful")
                .build();
    }

    public UserInfoRes register(RegisterReq request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new CustomAppException("Email already exists", HttpStatus.CONFLICT);
        });

        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(newUser);

        return UserInfoRes.builder()
                .id(savedUser.getUserId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toList()))
                .build();
    }

    public UserInfoRes getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomAppException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomAppException("Unauthorized", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return UserInfoRes.builder()
                .id(userDetails.getId())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .avatarUrl(userDetails.getAvatarUrl())
                .build();
    }

    public void logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
    }
}