package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.RoleUpgradeRequest;
import net.ecommerce.springboot.model.RoleUpgradeStatus;

public interface RoleUpgradeRequestRepository extends JpaRepository<RoleUpgradeRequest, Integer> {

	boolean existsByUser_IduserAndStatus(Integer iduser, RoleUpgradeStatus status);

	List<RoleUpgradeRequest> findByStatusOrderByCreatedAtDesc(RoleUpgradeStatus status);

	Optional<RoleUpgradeRequest> findTopByUser_IduserOrderByCreatedAtDesc(Integer iduser);
}
