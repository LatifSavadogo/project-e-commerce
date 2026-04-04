package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByName(String name);

	// Vérifier si un rôle existe
	boolean existsByName(String name);

	// Recherche par nom (insensible à la casse)
	Optional<Role> findByNameIgnoreCase(String name);

	// Recherche par nom contenant
	List<Role> findByNameContainingIgnoreCase(String name);

	// Requête personnalisée si besoin
	@Query("SELECT r FROM Role r WHERE r.name = :name")
	Optional<Role> findRoleByName(@Param("name") String name);
}
