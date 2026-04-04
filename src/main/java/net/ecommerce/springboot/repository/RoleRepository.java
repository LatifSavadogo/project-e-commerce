package net.ecommerce.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByLibroleIgnoreCase(String librole);

	boolean existsByLibroleIgnoreCase(String librole);
}
