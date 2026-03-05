package com.qridaba.qridabaplatform.service.users;

import com.qridaba.qridabaplatform.exception.EmailAlreadyExistsException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.exception.RoleNotAllowedException;
import com.qridaba.qridabaplatform.mapper.UserMapper;
import com.qridaba.qridabaplatform.model.dto.request.UserRequest;
import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.util.PasswordGeneratorUtil;
import com.qridaba.qridabaplatform.util.MailService;
import com.qridaba.qridabaplatform.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public List<UserResponse> getAllUsersIncludingDeleted() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public  List<UserResponse> getAllActiveUsers(){
        return userRepository.findAllByDeletedFalse().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getUsersByRole(String roleName) {
        return userRepository.findByRoles_Name(roleName).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }


    @Override
    public UserResponse getUserByEmail(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
       return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("Veuillez sélectionner au moins un rôle pour l'utilisateur.");
        }

        User creator = securityUtils.getCurrentUser();
        boolean isSuperAdmin = securityUtils.isSuperAdmin();

        Set<Role> rolesToAssign = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

        if (rolesToAssign.size() != request.getRoleIds().size()) {
            throw new ResourceNotFoundException("Certains rôles spécifiés n'existent pas dans le système.");
        }

        for (Role role : rolesToAssign) {
            if (role.getName().equals("SUPER_ADMIN")) {
                throw new RoleNotAllowedException("Action interdite : Il ne peut y avoir qu'un seul Super Admin.");
            }
            if (!isSuperAdmin && role.getName().equals("ADMIN")) {
                throw new RoleNotAllowedException("Accès refusé : Un Admin ne peut pas créer un autre compte Admin.");            }
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("L'adresse email '" + request.getEmail() + "' est déjà utilisée.");
        }

        String rawPassword = PasswordGeneratorUtil.generateRandomPassword(12);


        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .roles(rolesToAssign)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);


        mailService.sendCredentialsEmail(savedUser.getEmail(), rawPassword, savedUser.getFirstName());

        return userMapper.toResponse(savedUser);
    }


    @Override
    @Transactional
    public String hardDeleteUser(UUID id) {
        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isSuperAdmin = creator.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPER_ADMIN"));

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur avec l'ID " + id + " n'existe pas."));

        boolean targetIsSuperAdmin = userToDelete.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPER_ADMIN"));

        boolean targetIsAdmin = userToDelete.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));


        if (targetIsSuperAdmin) {
            throw new RoleNotAllowedException("Impossible de supprimer le compte SUPER_ADMIN.");
        }


        if (!isSuperAdmin && targetIsAdmin) {
            throw new RoleNotAllowedException("Accès refusé : Un Admin ne peut pas supprimer un autre Admin.");
        }

        try {
            userRepository.delete(userToDelete);
            return "L'utilisateur " + userToDelete.getEmail() + " a été supprimé avec succès.";
        } catch (Exception e) {

            throw new RuntimeException("Erreur technique lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String softDeleteUser(UUID id) {
        User creator = securityUtils.getCurrentUser();
        boolean isSuperAdmin = securityUtils.isSuperAdmin();

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'utilisateur avec l'ID " + id + " n'existe pas."));

        boolean targetIsSuperAdmin = userToDelete.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPER_ADMIN"));

        boolean targetIsAdmin = userToDelete.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));


        if (targetIsSuperAdmin) {
            throw new RoleNotAllowedException("Impossible de supprimer le compte SUPER_ADMIN.");
        }


        if (!isSuperAdmin && targetIsAdmin) {
            throw new RoleNotAllowedException("Accès refusé : Un Admin ne peut pas supprimer un autre Admin.");
        }

        try {
            userToDelete.softDelete();
            userToDelete.setEnabled(false);
            userRepository.save(userToDelete);
            return "L'utilisateur " + userToDelete.getEmail() + " a été supprimé avec succès.";
        } catch (Exception e) {

            throw new RuntimeException("Erreur technique lors de la suppression : " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public UserResponse toggleUserStatus(UUID id, boolean enabled) {

        User creator = securityUtils.getCurrentUser();
        boolean isSuperAdmin = securityUtils.isSuperAdmin();


        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));


        boolean targetIsSuperAdmin = targetUser.getRoles().stream().anyMatch(r -> r.getName().equals("SUPER_ADMIN"));
        boolean targetIsAdmin = targetUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));


        if (targetIsSuperAdmin) {
            throw new RoleNotAllowedException("Action interdite : Impossible de modifier le statut du Super Admin.");
        }

        if (!isSuperAdmin && targetIsAdmin) {
            throw new RoleNotAllowedException("Accès refusé : Un Admin ne peut pas modifier le statut d'un autre Admin.");
        }


        targetUser.setEnabled(enabled);
        User updatedUser = userRepository.save(targetUser);

        return userMapper.toResponse(updatedUser);
    }
    @Override
    @Transactional
    public UserResponse updateUserRoles(UUID id, List<UUID> roleIds) {

        User creator = securityUtils.getCurrentUser();
        boolean isSuperAdmin = securityUtils.isSuperAdmin();

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));


        Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(roleIds));
        if (newRoles.isEmpty()) {
            throw new IllegalArgumentException("Veuillez sélectionner au moins un rôle valide.");
        }


        if (targetUser.getRoles().stream().anyMatch(r -> r.getName().equals("SUPER_ADMIN"))) {
            throw new RoleNotAllowedException("Action interdite : Les rôles du Super Admin ne peuvent pas être modifiés.");
        }

        if (!isSuperAdmin) {
            boolean hasAdminRole = newRoles.stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("SUPER_ADMIN"));
            if (hasAdminRole) {
                throw new RoleNotAllowedException("Accès refusé : Vous n'avez لا تملك الصلاحية لتعيين دور Admin.");
            }


            if (targetUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
                throw new RoleNotAllowedException("Accès refusé : Vous ne pouvez pas modifier les rôles d'un autre Admin.");
            }
        }


        targetUser.setRoles(newRoles);
        User updatedUser = userRepository.save(targetUser);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse restoreUser(UUID id) {

        if (!securityUtils.isSuperAdmin()) {
            throw new RoleNotAllowedException("Accès refusé : Seul le Super Admin يمكنه استعادة الحسابات المحذوفة.");
        }

        User userToRestore = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));

        userToRestore.restore();
        userToRestore.setEnabled(true);

        User restoredUser = userRepository.save(userToRestore);
        return userMapper.toResponse(restoredUser);
    }
    @Override
    public List<UserResponse> searchUsers(String query) {
        return List.of();
    }

}
