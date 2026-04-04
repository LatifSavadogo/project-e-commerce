//package net.ecommerce.springboot.config;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import net.ecommerce.springboot.repository.UserRepository;
//
//@Configuration
//public class DataInitializer {
//
//    @Bean
//    CommandLineRunner initPasswordEncryption(
//            UserRepository userRepository, 
//            PasswordEncoder passwordEncoder) {
//        
//        return args -> {
//            System.out.println("🔄 Vérification et hash des mots de passe...");
//            
//            userRepository.findAll().forEach(user -> {
//                String currentPassword = user.getPassword();
//                
//                // Vérifie si le mot de passe n'est pas déjà hashé avec BCrypt
//                if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
//                    System.out.println("🔐 Hash du mot de passe pour : " + user.getEmail());
//                    
//                    String hashedPassword = passwordEncoder.encode(currentPassword);
//                    user.setPassword(hashedPassword);
//                    userRepository.save(user);
//                    
//                    System.out.println("✅ Mot de passe hashé avec succès pour : " + user.getEmail());
//                } else {
//                    System.out.println("✓ Mot de passe déjà hashé pour : " + user.getEmail());
//                }
//            });
//            
//            System.out.println("✅ Migration des mots de passe terminée !");
//        };
//    }
//}