//package net.ecommerce.springboot.service;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import net.ecommerce.springboot.model.User;
//import net.ecommerce.springboot.repository.UserRepository;
//
//import java.util.Collections;
//import java.util.List;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    public UserDetailsServiceImpl(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    @Transactional
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
//
//        System.out.println("🔐 Authentification tentée pour: " + email);
//        System.out.println("📧 Email: " + user.getEmail());
//        System.out.println("🔑 Mot de passe (hashé): " + user.getPassword());
//        System.out.println("🎭 Rôle: " + (user.getRole() != null ? user.getRole().getLibrole() : "NULL"));
//
//        if (user.getRole() == null) {
//            throw new UsernameNotFoundException("Aucun rôle défini pour: " + email);
//        }
//
//        List<GrantedAuthority> authorities = Collections.singletonList(
//            new SimpleGrantedAuthority("ROLE_" + user.getRole().getLibrole())
//        );
//
//        return (UserDetails) new User(
//        );
//    }
//}