package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.model.Admin;
import com.repos.AdminRepo;

@Configuration
public class DefaultAdminInitialise {

	@Autowired
	private PasswordEncoder encoder;

	@Bean
	public CommandLineRunner createDefaultAdmin(AdminRepo repo, PasswordEncoder encoder) {
		return args -> {
			String email = "admin@example.com";
			String password = "admin123";

			try {
				if (!repo.existsByAdminEmail(email)) {
					Admin admin = new Admin();
					admin.setFullname("Admin");
					admin.setAdminEmail(email);
					admin.setPassword(encoder.encode(password));
					admin.setRole("admin");
					repo.save(admin);
					System.out.println("Admin created: " + email);
				} else {
					System.out.println("Admin already exists: " + email);
				}
			} catch (Exception e) {
				System.out.println("Error in creating admin: " + e.getMessage());
			}
		};
	}
}
