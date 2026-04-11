package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

	/** Optionnel : point de livraison pour cette commande (travail, etc.). Sinon = domicile du profil. */
	@DecimalMin("-90.0")
	@DecimalMax("90.0")
	private Double livraisonLatitude;

	@DecimalMin("-180.0")
	@DecimalMax("180.0")
	private Double livraisonLongitude;

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
