package net.ecommerce.springboot.dto;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

	/** Même lieu de dépôt pour toutes les lignes du panier ; optionnel (domicile sinon). */
	@DecimalMin("-90.0")
	@DecimalMax("90.0")
	private Double livraisonLatitude;

	@DecimalMin("-180.0")
	@DecimalMax("180.0")
	private Double livraisonLongitude;

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

	public Double getLivraisonLatitude() {
		return livraisonLatitude;
	}

	public void setLivraisonLatitude(Double livraisonLatitude) {
		this.livraisonLatitude = livraisonLatitude;
	}

	public Double getLivraisonLongitude() {
		return livraisonLongitude;
	}

	public void setLivraisonLongitude(Double livraisonLongitude) {
		this.livraisonLongitude = livraisonLongitude;
	}
}
