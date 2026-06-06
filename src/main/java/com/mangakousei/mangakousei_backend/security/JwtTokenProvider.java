package com.mangakousei.mangakousei_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-milliseconds}")
    private int jwtExpirationMs;

    @Value("${app.jwt.remember-me-expiration-milliseconds}")
    private int rememberMeExpirationMs;

    @Value("${app.jwt.refresh-expirationMs-milliseconds}")
    private int refreshExpirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private String buildToken(String email, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey());

        claims.forEach(builder::claim);

        return builder.compact();
    }

    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails;
        }
        throw new IllegalStateException("Invalid authentication");
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails  = extractUserDetails(authentication);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("id", customUser.getId());
            claims.put("email", customUser.getEmail());
            claims.put("fullName", customUser.getFullName());
        }

        return buildToken(userDetails.getUsername(), claims, jwtExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication, boolean isRememberMe) {
        UserDetails user = extractUserDetails(authentication);
        long expirationMs = isRememberMe ? rememberMeExpirationMs : refreshExpirationMs;

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");

        return buildToken(user.getUsername(), claims, expirationMs);
    }

    public String generateToken(Authentication authentication, boolean isRememberMe) {
        if (authentication.getPrincipal() instanceof UserDetails userPrincipal) {

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return generateTokenFromEmail(userPrincipal.getUsername(), roles, isRememberMe);
        }
        throw new IllegalStateException("Cannot create token: authentication is invalid or empty");
    }

    public String generateTokenFromEmail(String email, List<String> roles, boolean isRememberMe) {
        Date now = new Date();
        long expirationMs = isRememberMe ? rememberMeExpirationMs : jwtExpirationMs;
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public long getExpirationRemaining(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}