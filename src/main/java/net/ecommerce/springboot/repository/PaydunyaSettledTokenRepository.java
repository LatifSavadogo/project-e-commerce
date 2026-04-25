package net.ecommerce.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.PaydunyaSettledToken;

public interface PaydunyaSettledTokenRepository extends JpaRepository<PaydunyaSettledToken, String> {
}
