package com.example.outsourcingproject.global.config;

import com.example.outsourcingproject.global.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return Optional.of(userPrincipal.getId());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
