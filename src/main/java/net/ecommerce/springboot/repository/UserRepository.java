package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.User;


public interface UserRepository extends JpaRepository<User, Integer> {

boolean existsByEmail(String email);
    
    List<User> findByRoleIdrole(Integer idrole);
    
    List<User> findByNomContainingIgnoreCase(String nom);
    
    Optional<User> findByEmail(String email);
    
}
