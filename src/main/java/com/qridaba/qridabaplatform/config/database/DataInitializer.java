package com.qridaba.qridabaplatform.config.database;

import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.user.UserProfile;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        createRoles();
        createSuperAdmin();
        createAdmin();
        log.info("Data initialization completed.");
    }

    private void createRoles() {
        Arrays.asList("SUPER_ADMIN", "ADMIN", "USER").forEach(name -> {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(Role.builder().name(name).build());
                log.info("Created role: {}", name);
            }
        });
    }

    private void createSuperAdmin() {
        String email = "superadmin@gmail.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN not found"));

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("password123")) // Default password
                    .firstName("Super")
                    .lastName("Admin")
                    .enabled(true)
                    .roles(Set.of(superAdminRole, adminRole))
                    .build();

            UserProfile profile = UserProfile.builder()
                    .build();

            user.setProfile(profile);
            profile.setUser(user);

            userRepository.save(user);
            log.info("Created SuperAdmin user: {}", email);
        }
    }

    private void createAdmin() {
        String email = "admin@gmail.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("password123")) // Default password
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();

            UserProfile profile = UserProfile.builder()
                    .build();

            user.setProfile(profile);
            profile.setUser(user);

            userRepository.save(user);
            log.info("Created Admin user: {}", email);
        }
    }
}
