package net.ecommerce.springboot.dto;

public class VendorRatingSummaryDTO {

	private Integer idVendeur;
	private Double moyenneEtoiles;
	private long nombreAvis;
	private boolean certifie;

	public Integer getIdVendeur() {
		return idVendeur;
	}

	public void setIdVendeur(Integer idVendeur) {
		this.idVendeur = idVendeur;
	}

	public Double getMoyenneEtoiles() {
		return moyenneEtoiles;
	}

	public void setMoyenneEtoiles(Double moyenneEtoiles) {
		this.moyenneEtoiles = moyenneEtoiles;
	}

	public long getNombreAvis() {
		return nombreAvis;
	}

	public void setNombreAvis(long nombreAvis) {
		this.nombreAvis = nombreAvis;
	}

	public boolean isCertifie() {
		return certifie;
	}

	public void setCertifie(boolean certifie) {
		this.certifie = certifie;
	}
}
