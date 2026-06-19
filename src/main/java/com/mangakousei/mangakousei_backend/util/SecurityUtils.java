package com.mangakousei.mangakousei_backend.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.security.CustomUserDetails;

import java.util.Objects;

public class SecurityUtils {
    
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new CustomAppException("User not authenticated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        
        throw new CustomAppException("Cannot extract user ID", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return auth.getAuthorities().stream()
            .anyMatch(grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), "ADMIN"));
    }

    public static boolean isMangaka() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), "MANGAKA"));
    }

    public static boolean isTantou() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), "TANTOU"));
    }

    public static boolean isAssistant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), "ASSISTANT"));
    }
}
