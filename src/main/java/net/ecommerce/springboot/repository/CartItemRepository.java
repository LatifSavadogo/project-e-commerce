package net.ecommerce.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

	Optional<CartItem> findByIdcartitemAndCart_User_Iduser(Integer idcartitem, Integer iduser);

	boolean existsByAgreedMessage_Idmessage(Integer idmessage);
}
