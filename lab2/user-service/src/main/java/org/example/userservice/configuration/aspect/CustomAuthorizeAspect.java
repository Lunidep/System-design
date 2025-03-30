package org.example.userservice.configuration.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CustomAuthorizeAspect {

    @Before("@annotation(customAuthorize)")
    public void checkAuthorization(JoinPoint joinPoint, CustomAuthorize customAuthorize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Пользователь не аутентифицирован");
        }

        String[] requiredRoles = customAuthorize.value();
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    for (String role : requiredRoles) {
                        if (authority.getAuthority().equals(role)) {
                            return true;
                        }
                    }
                    return false;
                });

        if (!hasRole) {
            throw new SecurityException("Доступ запрещен: недостаточно прав");
        }
    }
}

