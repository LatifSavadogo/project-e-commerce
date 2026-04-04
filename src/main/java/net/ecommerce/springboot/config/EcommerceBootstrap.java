package net.ecommerce.springboot.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.RoleRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Configuration
public class EcommerceBootstrap {

	private static final Logger log = LoggerFactory.getLogger(EcommerceBootstrap.class);

	@Value("${app.security.bootstrap-super-admin-password}")
	private String bootstrapSuperAdminPassword;

	@Value("${app.security.bootstrap-livreur-password}")
	private String bootstrapLivreurPassword;

	@Bean
	@Order(0)
	CommandLineRunner seedRolesAndSuperAdmins(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			ensureRole(roleRepository, RoleNames.ACHETEUR, "Compte acheteur");
			ensureRole(roleRepository, RoleNames.VENDEUR, "Compte vendeur");
			ensureRole(roleRepository, RoleNames.LIVREUR, "Livreur");
			ensureRole(roleRepository, RoleNames.ADMIN, "Administrateur");
			ensureRole(roleRepository, RoleNames.SUPER_ADMIN, "Super administrateur");

			Role superRole = roleRepository.findByLibroleIgnoreCase(RoleNames.SUPER_ADMIN).orElseThrow();
			ensureSuperAdmin(userRepository, passwordEncoder, superRole, "latif@admin.com");
			ensureSuperAdmin(userRepository, passwordEncoder, superRole, "pare@admin.com");

			Role livreurRole = roleRepository.findByLibroleIgnoreCase(RoleNames.LIVREUR).orElseThrow();
			ensureDemoLivreur(userRepository, passwordEncoder, livreurRole, "livreur@demo.ecom");
		};
	}

	private void ensureRole(RoleRepository roleRepository, String lib, String desc) {
		if (roleRepository.findByLibroleIgnoreCase(lib).isEmpty()) {
			Role r = new Role();
			r.setLibrole(lib);
			r.setDescrole(desc);
			r.setUserupdate("system");
			r.setDateupdate(LocalDateTime.now());
			roleRepository.save(r);
			log.info("Rôle créé : {}", lib);
		}
	}

	private void ensureSuperAdmin(UserRepository userRepository, PasswordEncoder encoder, Role superRole,
			String email) {
		if (userRepository.findByEmail(email).isPresent()) {
			return;
		}
		User u = new User();
		u.setNom("Admin");
		u.setPrenom(email.contains("latif") ? "Latif" : "Pare");
		u.setEmail(email);
		u.setPassword(encoder.encode(bootstrapSuperAdminPassword));
		u.setRole(superRole);
		u.setUserupdate("bootstrap");
		u.setDateupdate(LocalDateTime.now());
		userRepository.save(u);
		log.warn("Compte super-admin créé : {} — changez le mot de passe (variable BOOTSTRAP_SUPER_ADMIN_PASSWORD).",
				email);
	}

	private void ensureDemoLivreur(UserRepository userRepository, PasswordEncoder encoder, Role livreurRole,
			String email) {
		if (userRepository.findByEmail(email).isPresent()) {
			return;
		}
		User u = new User();
		u.setNom("Demo");
		u.setPrenom("Livreur");
		u.setEmail(email);
		u.setPassword(encoder.encode(bootstrapLivreurPassword));
		u.setRole(livreurRole);
		u.setTypeEnginLivreur(TypeEnginLivreur.MOTO);
		u.setUserupdate("bootstrap");
		u.setDateupdate(LocalDateTime.now());
		userRepository.save(u);
		log.warn("Compte livreur démo créé : {} — mot de passe : variable BOOTSTRAP_LIVREUR_PASSWORD.",
				email);
	}
}
