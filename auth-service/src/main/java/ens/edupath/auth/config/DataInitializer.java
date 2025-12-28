package ens.edupath.auth.config;

import ens.edupath.auth.entity.Role;
import ens.edupath.auth.repository.RoleRepository;
import ens.edupath.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserService userService;

    public DataInitializer(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Initialiser les rôles
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName(Role.RoleName.ROLE_STUDENT).isEmpty()) {
            Role studentRole = new Role();
            studentRole.setName(Role.RoleName.ROLE_STUDENT);
            roleRepository.save(studentRole);
        }

        // Initialiser l'admin par défaut
        userService.initializeDefaultAdmin();
    }
}


