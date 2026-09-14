package com.messatto.config;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.enums.Role;
import com.messatto.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedDefaultAdmin;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-default-super-admin:true}") boolean seedDefaultAdmin
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedDefaultAdmin = seedDefaultAdmin;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedDefaultAdmin || userRepository.existsByEmailIgnoreCase("superadmin@messatto.local")) {
            return;
        }

        AppUser superAdmin = new AppUser(
                "Default Super Admin",
                "superadmin@messatto.local",
                null,
                passwordEncoder.encode("SuperAdmin@123"),
                Role.SUPER_ADMIN,
                "Main Hostel",
                true
        );
        userRepository.save(superAdmin);
        log.info("Seeded default super admin account: superadmin@messatto.local");
    }
}
