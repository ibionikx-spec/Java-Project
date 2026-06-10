package com.mangakousei.mangakousei_backend.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieService {

    private final boolean isSecure = false;
    private final String sameSite = "Lax";
    private final String accessTokenPath = "/";
    private final String refreshTokenPath = "/api/auth/refresh";

    private ResponseCookie buildCookie(String name, String value, String path, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isSecure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    public void setAuthBothCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken,
            long refreshMaxAge
    ) {

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(isSecure)
                .path(accessTokenPath)
                .maxAge(60 * 15)
                .sameSite(sameSite)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .path(refreshTokenPath)
                .maxAge(refreshMaxAge)
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        clearAuthCookies(response, "accessToken");
        clearAuthCookies(response, "refreshToken");
    }

    public void clearAuthCookies(HttpServletResponse response, String cookieName) {
        String tokenPath = cookieName.equals("accessToken") ? accessTokenPath : refreshTokenPath;
        ResponseCookie deleteCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(isSecure)
                .path(tokenPath)
                .maxAge(0)
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}