package com.example.outsourcingproject.global.aop.aspect.util;

import com.example.outsourcingproject.global.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ActivityLogAspectUtil {

    private ActivityLogAspectUtil() {}

    public static Long getUserIdFromUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }
        }

        return null;
    }
}
