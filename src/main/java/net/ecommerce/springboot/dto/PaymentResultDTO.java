package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.PaymentMethod;
import net.ecommerce.springboot.util.VendorOrderReferenceCodec;

public class PaymentResultDTO {

	private Integer idtransaction;
	private Integer idArticle;
	private String articleLibelle;
	private int quantite;
	private int prixUnitaire;
	private int montantTotal;
	private int frais;
	private PaymentMethod moyenPaiement;
	private LocalDateTime datecreation;
	private String message;
	private Integer idLivraison;
	private String livraisonStatut;
	private String livraisonTypeEngin;
	/** Visible seulement vendeur / staff (pas l’acheteur). */
	private String vendorPickupCode;
	/** Métadonnées commande encodées Base64 (sans montant) — vendeur / staff. */
	private String vendorPackedReferenceBase64;

	public static PaymentResultDTO fromEntity(EcomTransaction t, boolean exposeVendorSecrets) {
		PaymentResultDTO d = new PaymentResultDTO();
		d.setIdtransaction(t.getIdtransaction());
		if (t.getArticle() != null) {
			d.setIdArticle(t.getArticle().getIdarticle());
			d.setArticleLibelle(t.getArticle().getLibarticle());
		}
		d.setQuantite(t.getQuantite());
		d.setPrixUnitaire(t.getPrixUnitaireSnapshot());
		d.setMontantTotal(t.getMontantTotal());
		d.setFrais(t.getFraisAffiches());
		d.setMoyenPaiement(t.getMoyenPaiement());
		d.setDatecreation(t.getDatecreation());
		d.setMessage("Paiement enregistré. Téléchargez le reçu via GET /api/v1/payments/{id}/receipt");
		Livraison liv = t.getLivraison();
		if (liv != null) {
			d.setIdLivraison(liv.getIdlivraison());
			d.setLivraisonStatut(liv.getStatut().name());
			if (liv.getTypeEnginUtilise() != null) {
				d.setLivraisonTypeEngin(liv.getTypeEnginUtilise().name());
			}
			if (exposeVendorSecrets && liv.getVendorPickupCode() != null) {
				d.setVendorPickupCode(liv.getVendorPickupCode());
				d.setVendorPackedReferenceBase64(VendorOrderReferenceCodec.encode(t, liv, liv.getVendorPickupCode()));
			}
		}
		return d;
	}

	public Integer getIdtransaction() {
		return idtransaction;
	}

	public void setIdtransaction(Integer idtransaction) {
		this.idtransaction = idtransaction;
	}

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public String getArticleLibelle() {
		return articleLibelle;
	}

	public void setArticleLibelle(String articleLibelle) {
		this.articleLibelle = articleLibelle;
	}

	public int getQuantite() {
		return quantite;
	}

	public void setQuantite(int quantite) {
		this.quantite = quantite;
	}

	public int getPrixUnitaire() {
		return prixUnitaire;
	}

	public void setPrixUnitaire(int prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}

	public int getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(int montantTotal) {
		this.montantTotal = montantTotal;
	}

	public int getFrais() {
		return frais;
	}

	public void setFrais(int frais) {
		this.frais = frais;
	}

	public PaymentMethod getMoyenPaiement() {
		return moyenPaiement;
	}

	public void setMoyenPaiement(PaymentMethod moyenPaiement) {
		this.moyenPaiement = moyenPaiement;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getIdLivraison() {
		return idLivraison;
	}

	public void setIdLivraison(Integer idLivraison) {
		this.idLivraison = idLivraison;
	}

	public String getLivraisonStatut() {
		return livraisonStatut;
	}

	public void setLivraisonStatut(String livraisonStatut) {
		this.livraisonStatut = livraisonStatut;
	}

	public String getLivraisonTypeEngin() {
		return livraisonTypeEngin;
	}

	public void setLivraisonTypeEngin(String livraisonTypeEngin) {
		this.livraisonTypeEngin = livraisonTypeEngin;
	}

	public String getVendorPickupCode() {
		return vendorPickupCode;
	}

	public void setVendorPickupCode(String vendorPickupCode) {
		this.vendorPickupCode = vendorPickupCode;
	}

	public String getVendorPackedReferenceBase64() {
		return vendorPackedReferenceBase64;
	}

	public void setVendorPackedReferenceBase64(String vendorPackedReferenceBase64) {
		this.vendorPackedReferenceBase64 = vendorPackedReferenceBase64;
	}
}
