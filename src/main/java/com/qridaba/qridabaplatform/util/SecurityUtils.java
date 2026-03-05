package com.qridaba.qridabaplatform.util;

import com.qridaba.qridabaplatform.model.entity.user.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean isSuperAdmin() {
        return getCurrentUser().getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPER_ADMIN"));
    }
}