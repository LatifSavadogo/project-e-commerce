package net.ecommerce.springboot.dto;

/**
 * Mise à jour partielle par un administrateur (sans toucher au mot de passe).
 * Champs absents ou {@code null} : inchangés.
 */
public class AdminUserPatchDTO {

	private Integer idrole;
	private Integer idpays;
	private Integer idtypeVendeur;

	/** MOTO ou VEHICULE ; réservé aux comptes livreur. */
	private String typeEnginLivreur;

	public Integer getIdrole() {
		return idrole;
	}

	public void setIdrole(Integer idrole) {
		this.idrole = idrole;
	}

	public Integer getIdpays() {
		return idpays;
	}

	public void setIdpays(Integer idpays) {
		this.idpays = idpays;
	}

	public Integer getIdtypeVendeur() {
		return idtypeVendeur;
	}

	public void setIdtypeVendeur(Integer idtypeVendeur) {
		this.idtypeVendeur = idtypeVendeur;
	}

	public String getTypeEnginLivreur() {
		return typeEnginLivreur;
	}

	public void setTypeEnginLivreur(String typeEnginLivreur) {
		this.typeEnginLivreur = typeEnginLivreur;
	}
}
