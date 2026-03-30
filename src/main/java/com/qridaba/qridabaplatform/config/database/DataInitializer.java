package com.qridaba.qridabaplatform.config.database;

import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.model.entity.user.UserProfile;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.model.entity.item.Item;
import com.qridaba.qridabaplatform.model.entity.item.ItemImage;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import com.qridaba.qridabaplatform.repository.ItemRepository;
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
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        createRoles();
        createSuperAdmin();
        createAdmin();
        createOwner();
        createClient();
        createCategories();
        log.info("Data initialization completed.");
    }

    private void createRoles() {
        Arrays.asList("SUPER_ADMIN", "ADMIN", "USER", "OWNER", "CLIENT").forEach(name -> {
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

    private void createOwner() {
        String email = "owner@gmail.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            Role ownerRole = roleRepository.findByName("OWNER")
                    .orElseThrow(() -> new RuntimeException("Role OWNER not found"));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Demo")
                    .lastName("Owner")
                    .enabled(true)
                    .roles(Set.of(ownerRole))
                    .build();

            UserProfile profile = UserProfile.builder().build();
            user.setProfile(profile);
            profile.setUser(user);

            userRepository.save(user);
            log.info("Created Owner user: {}", email);
        }
    }

    private void createClient() {
        String email = "clinte@gmail.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            Role clientRole = roleRepository.findByName("CLIENT")
                    .orElseThrow(() -> new RuntimeException("Role CLIENT not found"));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Demo")
                    .lastName("Client")
                    .enabled(true)
                    .roles(Set.of(clientRole))
                    .build();

            UserProfile profile = UserProfile.builder().build();
            user.setProfile(profile);
            profile.setUser(user);

            userRepository.save(user);
            log.info("Created Client user: {}", email);
        }
    }

    private void createCategories() {
        List<Category> categories = Arrays.asList(
            Category.builder().name("Electronics & Computers").description("Laptops, PCs, tablets, and components").icon("lucide:laptop").build(),
            Category.builder().name("Smartphones & Wearables").description("Phones, smartwatches, and accessories").icon("lucide:smartphone").build(),
            Category.builder().name("Photography & Video").description("Cameras, lenses, drones, and lighting").icon("lucide:camera").build(),
            Category.builder().name("Audio & Music").description("Speakers, headphones, DJ gear, instruments").icon("lucide:headphones").build(),
            Category.builder().name("Gaming Consoles & VR").description("PlayStation, Xbox, Nintendo, VR headsets").icon("lucide:gamepad-2").build(),
            Category.builder().name("Cars & Vehicles").description("Rental cars, vans, and automotive tools").icon("lucide:car").build(),
            Category.builder().name("Motorcycles & Scooters").description("Bikes, electric scooters, and gear").icon("lucide:bike").build(),
            Category.builder().name("Bicycles & Skating").description("Mountain bikes, city cycles, and skateboards").icon("lucide:person-standing").build(),
            Category.builder().name("Sports & Fitness").description("Gym equipment, camping gear, and surfboards").icon("lucide:dumbbell").build(),
            Category.builder().name("Tools & DIY").description("Power tools, drills, and construction gear").icon("lucide:hammer").build(),
            Category.builder().name("Home & Furniture").description("Sofas, tables, and interior decorations").icon("lucide:sofa").build(),
            Category.builder().name("Appliances").description("Refrigerators, washing machines, microwaves").icon("lucide:refrigerator").build(),
            Category.builder().name("Fashion & Clothing").description("Dresses, suits, costumes, and accessories").icon("lucide:shirt").build(),
            Category.builder().name("Jewelry & Watches").description("Luxury watches, rings, and necklaces").icon("lucide:watch").build(),
            Category.builder().name("Kids & Baby").description("Toys, strollers, car seats, and cribs").icon("lucide:baby").build(),
            Category.builder().name("Party & Events").description("Tents, catering equipment, and decorations").icon("lucide:tent").build(),
            Category.builder().name("Books & Education").description("Textbooks, encyclopedias, and supplies").icon("lucide:book-open").build(),
            Category.builder().name("Medical & Health").description("Wheelchairs, crutches, and monitors").icon("lucide:stethoscope").build(),
            Category.builder().name("Real Estate").description("Apartments, workspaces, and venues").icon("lucide:building").build(),
            Category.builder().name("Services & Other").description("Freelancers, heavy machinery, and miscellaneous").icon("lucide:briefcase").build()
        );

        categories.forEach(newCat -> {
            categoryRepository.findByName(newCat.getName()).ifPresentOrElse(
                existingCat -> {
                    // Update existing to ensure correct lucide icons and descriptions
                    existingCat.setIcon(newCat.getIcon());
                    existingCat.setDescription(newCat.getDescription());
                    categoryRepository.save(existingCat);
                    log.info("Updated existing category: {}", existingCat.getName());
                },
                () -> {
                    categoryRepository.save(newCat);
                    log.info("Created new category: {}", newCat.getName());
                }
            );
        });
    }
  
}
