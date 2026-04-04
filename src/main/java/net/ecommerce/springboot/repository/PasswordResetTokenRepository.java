package net.ecommerce.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("DELETE FROM PasswordResetToken t WHERE t.user.iduser = :iduser AND t.usedAt IS NULL")
	int deleteUnusedByUserId(@Param("iduser") Integer iduser);
}
