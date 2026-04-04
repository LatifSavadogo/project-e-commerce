package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.ecommerce.springboot.model.PaymentMethod;

public class PaymentCreateDTO {

	@NotNull
	private Integer idArticle;

	@NotNull
	@Min(1)
	@Max(100)
	private Integer quantite;

	@NotNull
	private PaymentMethod moyenPaiement;

	@NotBlank
	@Size(max = 200)
	private String referenceExterne;

	/**
	 * Prix unitaire issu d’une offre **acceptée** (même acheteur, vendeur, article). Sinon laisser null pour
	 * utiliser le prix catalogue.
	 */
	@Min(1)
	private Integer prixUnitaireNegocie;

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}

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

	public Integer getPrixUnitaireNegocie() {
		return prixUnitaireNegocie;
	}

	public void setPrixUnitaireNegocie(Integer prixUnitaireNegocie) {
		this.prixUnitaireNegocie = prixUnitaireNegocie;
	}
}
