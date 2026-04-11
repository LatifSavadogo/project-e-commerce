package net.ecommerce.springboot.dto;

import java.util.ArrayList;
import java.util.List;

/** Suivi commande / livraison pour l’acheteur ou le staff. */
public class CommandeSuiviDTO {

	private Integer idtransaction;
	private Integer idlivraison;
	private String statutLivraison;
	private String articleLibelle;
	private Integer quantite;
	private Integer montantTotal;
	private List<SuiviEtapeDTO> etapes = new ArrayList<>();
	/** Présent seulement pour vendeur / admin. */
	private String vendorPickupCode;
	private String vendorPackedReferenceBase64;
	private String livreurPrenom;
	private String livreurNom;
	private boolean livreurAssigne;
	/** L’acheteur peut ouvrir Maps quand un livreur a pris la course. */
	private boolean navigationDisponible;
	private String lienRetraitChezVendeur;
	private String lienLivraisonChezClient;
	private String lienTrajetVendeurVersClient;
	/** Itinéraire depuis la position du livreur (profil ou dernière position partagée) vers le client, si calculable. */
	private String lienLivreurVersClient;
	/** Horodatage ISO de la dernière position livreur utilisée pour le lien (si issue du partage GPS). */
	private String livreurPositionMiseAJourAt;

	public Integer getIdtransaction() {
		return idtransaction;
	}

	public void setIdtransaction(Integer idtransaction) {
		this.idtransaction = idtransaction;
	}

	public Integer getIdlivraison() {
		return idlivraison;
	}

	public void setIdlivraison(Integer idlivraison) {
		this.idlivraison = idlivraison;
	}

	public String getStatutLivraison() {
		return statutLivraison;
	}

	public void setStatutLivraison(String statutLivraison) {
		this.statutLivraison = statutLivraison;
	}

	public String getArticleLibelle() {
		return articleLibelle;
	}

	public void setArticleLibelle(String articleLibelle) {
		this.articleLibelle = articleLibelle;
	}

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}

	public Integer getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(Integer montantTotal) {
		this.montantTotal = montantTotal;
	}

	public List<SuiviEtapeDTO> getEtapes() {
		return etapes;
	}

	public void setEtapes(List<SuiviEtapeDTO> etapes) {
		this.etapes = etapes;
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

	public String getLivreurPrenom() {
		return livreurPrenom;
	}

	public void setLivreurPrenom(String livreurPrenom) {
		this.livreurPrenom = livreurPrenom;
	}

	public String getLivreurNom() {
		return livreurNom;
	}

	public void setLivreurNom(String livreurNom) {
		this.livreurNom = livreurNom;
	}

	public boolean isLivreurAssigne() {
		return livreurAssigne;
	}

	public void setLivreurAssigne(boolean livreurAssigne) {
		this.livreurAssigne = livreurAssigne;
	}

	public boolean isNavigationDisponible() {
		return navigationDisponible;
	}

	public void setNavigationDisponible(boolean navigationDisponible) {
		this.navigationDisponible = navigationDisponible;
	}

	public String getLienRetraitChezVendeur() {
		return lienRetraitChezVendeur;
	}

	public void setLienRetraitChezVendeur(String lienRetraitChezVendeur) {
		this.lienRetraitChezVendeur = lienRetraitChezVendeur;
	}

	public String getLienLivraisonChezClient() {
		return lienLivraisonChezClient;
	}

	public void setLienLivraisonChezClient(String lienLivraisonChezClient) {
		this.lienLivraisonChezClient = lienLivraisonChezClient;
	}

	public String getLienTrajetVendeurVersClient() {
		return lienTrajetVendeurVersClient;
	}

	public void setLienTrajetVendeurVersClient(String lienTrajetVendeurVersClient) {
		this.lienTrajetVendeurVersClient = lienTrajetVendeurVersClient;
	}

	public String getLienLivreurVersClient() {
		return lienLivreurVersClient;
	}

	public void setLienLivreurVersClient(String lienLivreurVersClient) {
		this.lienLivreurVersClient = lienLivreurVersClient;
	}

	public String getLivreurPositionMiseAJourAt() {
		return livreurPositionMiseAJourAt;
	}

	public void setLivreurPositionMiseAJourAt(String livreurPositionMiseAJourAt) {
		this.livreurPositionMiseAJourAt = livreurPositionMiseAJourAt;
	}
}
