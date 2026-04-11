package net.ecommerce.springboot.dto;

import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.util.NavigationUrls;

/**
 * Vue livreur : pas de montants ni prix.
 */
public class LivraisonLivreurDTO {

	private Integer idlivraison;
	private Integer idtransaction;
	private String statut;
	private String typeEnginUtilise;
	private Integer idlivreur;
	private String livreurEmail;
	private String livreurNomComplet;
	private Integer idArticle;
	private String articleLibelle;
	private Integer quantite;
	private String acheteurEmail;
	private String acheteurVille;
	private String vendeurEmail;
	private String datecreation;
	private String datePriseEnCharge;
	private String dateLivraison;
	private LivreurNavigationDTO navigation;
	/** Lien Google Maps (recherche lat,lng) vers le point de dépôt client (commande ou domicile acheteur). */
	private String lieuDepotCarteUrl;

	public static LivraisonLivreurDTO fromEntity(Livraison l) {
		LivraisonLivreurDTO d = new LivraisonLivreurDTO();
		d.setIdlivraison(l.getIdlivraison());
		d.setStatut(l.getStatut().name());
		if (l.getTypeEnginUtilise() != null) {
			d.setTypeEnginUtilise(l.getTypeEnginUtilise().name());
		}
		if (l.getDatecreation() != null) {
			d.setDatecreation(l.getDatecreation().toString());
		}
		if (l.getDatePriseEnCharge() != null) {
			d.setDatePriseEnCharge(l.getDatePriseEnCharge().toString());
		}
		if (l.getDateLivraison() != null) {
			d.setDateLivraison(l.getDateLivraison().toString());
		}
		if (l.getLivreur() != null) {
			d.setIdlivreur(l.getLivreur().getIduser());
			d.setLivreurEmail(l.getLivreur().getEmail());
			d.setLivreurNomComplet(l.getLivreur().getPrenom() + " " + l.getLivreur().getNom());
		}
		EcomTransaction t = l.getTransaction();
		if (t != null) {
			d.setIdtransaction(t.getIdtransaction());
			d.setQuantite(t.getQuantite());
			if (t.getArticle() != null) {
				d.setIdArticle(t.getArticle().getIdarticle());
				d.setArticleLibelle(t.getArticle().getLibarticle());
			}
			if (t.getAcheteur() != null) {
				d.setAcheteurEmail(t.getAcheteur().getEmail());
				d.setAcheteurVille(t.getAcheteur().getVille());
			}
			if (t.getVendeur() != null) {
				d.setVendeurEmail(t.getVendeur().getEmail());
			}
			Double lat = t.getLivraisonLatitude();
			Double lng = t.getLivraisonLongitude();
			if (lat == null || lng == null) {
				User acheteur = t.getAcheteur();
				if (acheteur != null) {
					lat = acheteur.getLatitude();
					lng = acheteur.getLongitude();
				}
			}
			if (lat != null && lng != null) {
				d.setLieuDepotCarteUrl(NavigationUrls.googleMapsSearchLatLng(lat, lng));
			}
		}
		return d;
	}

	public Integer getIdlivraison() {
		return idlivraison;
	}

	public void setIdlivraison(Integer idlivraison) {
		this.idlivraison = idlivraison;
	}

	public Integer getIdtransaction() {
		return idtransaction;
	}

	public void setIdtransaction(Integer idtransaction) {
		this.idtransaction = idtransaction;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getTypeEnginUtilise() {
		return typeEnginUtilise;
	}

	public void setTypeEnginUtilise(String typeEnginUtilise) {
		this.typeEnginUtilise = typeEnginUtilise;
	}

	public Integer getIdlivreur() {
		return idlivreur;
	}

	public void setIdlivreur(Integer idlivreur) {
		this.idlivreur = idlivreur;
	}

	public String getLivreurEmail() {
		return livreurEmail;
	}

	public void setLivreurEmail(String livreurEmail) {
		this.livreurEmail = livreurEmail;
	}

	public String getLivreurNomComplet() {
		return livreurNomComplet;
	}

	public void setLivreurNomComplet(String livreurNomComplet) {
		this.livreurNomComplet = livreurNomComplet;
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

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}

	public String getAcheteurEmail() {
		return acheteurEmail;
	}

	public void setAcheteurEmail(String acheteurEmail) {
		this.acheteurEmail = acheteurEmail;
	}

	public String getAcheteurVille() {
		return acheteurVille;
	}

	public void setAcheteurVille(String acheteurVille) {
		this.acheteurVille = acheteurVille;
	}

	public String getVendeurEmail() {
		return vendeurEmail;
	}

	public void setVendeurEmail(String vendeurEmail) {
		this.vendeurEmail = vendeurEmail;
	}

	public String getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(String datecreation) {
		this.datecreation = datecreation;
	}

	public String getDatePriseEnCharge() {
		return datePriseEnCharge;
	}

	public void setDatePriseEnCharge(String datePriseEnCharge) {
		this.datePriseEnCharge = datePriseEnCharge;
	}

	public String getDateLivraison() {
		return dateLivraison;
	}

	public void setDateLivraison(String dateLivraison) {
		this.dateLivraison = dateLivraison;
	}

	public LivreurNavigationDTO getNavigation() {
		return navigation;
	}

	public void setNavigation(LivreurNavigationDTO navigation) {
		this.navigation = navigation;
	}

	public boolean isNavigationPossible() {
		return statut != null && LivraisonStatut.EN_COURS.name().equals(statut) && navigation != null;
	}

	public String getLieuDepotCarteUrl() {
		return lieuDepotCarteUrl;
	}

	public void setLieuDepotCarteUrl(String lieuDepotCarteUrl) {
		this.lieuDepotCarteUrl = lieuDepotCarteUrl;
	}
}
