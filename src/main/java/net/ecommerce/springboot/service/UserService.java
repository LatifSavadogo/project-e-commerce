//package net.ecommerce.springboot.service;
//
//
//import net.ecommerce.springboot.model.User;
//import net.ecommerce.springboot.repository.UserRepository;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository userRepository, 
//                             PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Transactional
//    public User createUser(User user) {
//        if (user.getPassword() != null && 
//            !user.getPassword().startsWith("$2a$") && 
//            !user.getPassword().startsWith("$2b$")) {
//            
//        	user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//        return userRepository.save(user);
//    }
//
//    @Transactional
//    public User updatePassword(Integer iduser, String newPassword) {
//        User user = userRepository.findById(iduser)
//            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//        
//        user.setPassword(passwordEncoder.encode(newPassword));
//        return userRepository.save(user);
//    }
//}