package net.ecommerce.springboot.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.ecommerce.springboot.model.PaymentMethod;

public class CartCheckoutDTO {

	@NotNull
	private PaymentMethod moyenPaiement;

	@NotBlank
	@Size(max = 200)
	private String referenceExterne;

	@NotEmpty
	private List<Integer> cartItemIds;

	public PaymentMethod getMoyenPaiement() {
		return moyenPaiement;
	}

	public void setMoyenPaiement(PaymentMethod moyenPaiement) {
		this.moyenPaiement = moyenPaiement;
	}

	public String getReferenceExterne() {
		return referenceExterne;
	}

	public void setReferenceExterne(String referenceExterne) {
		this.referenceExterne = referenceExterne;
	}

	public List<Integer> getCartItemIds() {
		return cartItemIds;
	}

	public void setCartItemIds(List<Integer> cartItemIds) {
		this.cartItemIds = cartItemIds;
	}
}
