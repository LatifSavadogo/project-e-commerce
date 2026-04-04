//package net.ecommerce.springboot.service;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import net.ecommerce.springboot.model.User;
//import net.ecommerce.springboot.repository.UserRepository;
//
//@Service
//public class AuthService {
//
//    private final UserRepository userRepository;
//
//    public AuthService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetails) {
//                String email = ((UserDetails) principal).getUsername();
//                return userRepository.findByEmail(email).orElse(null);
//            }
//        }
//        return null;
//    }
//
//    public String getCurrentUsername() {
//        User user = getCurrentUser();
//        return user != null ? user.getNom() : "system";
//    }
//
//    public String getCurrentUserEmail() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserDetails) {
//                return ((UserDetails) principal).getUsername();
//            }
//        }
//        return null;
//    }
//
//    public boolean hasRole(String roleName) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            return authentication.getAuthorities().stream()
//                    .anyMatch(grantedAuthority -> 
//                        grantedAuthority.getAuthority().equals("ROLE_" + roleName));
//        }
//        return false;
//    }
//}