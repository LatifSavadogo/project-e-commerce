package net.ecommerce.springboot.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.ecommerce.springboot.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner hashPlainPasswordsIfNeeded(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			userRepository.findAll().forEach(user -> {
				String pwd = user.getPassword();
				if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$") && !pwd.startsWith("$2y$")) {
					user.setPassword(passwordEncoder.encode(pwd));
					userRepository.save(user);
				}
			});
		};
	}
}
