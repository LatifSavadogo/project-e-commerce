package net.ecommerce.springboot.dto;

/** Liens Maps pour la course (livreur : chez lui → vendeur → client). */
public class LivreurNavigationDTO {

	private String itineraireComplet;
	private String etapeRetraitVendeur;
	private String etapeDepotClient;

	public String getItineraireComplet() {
		return itineraireComplet;
	}

	public void setItineraireComplet(String itineraireComplet) {
		this.itineraireComplet = itineraireComplet;
	}

	public String getEtapeRetraitVendeur() {
		return etapeRetraitVendeur;
	}

	public void setEtapeRetraitVendeur(String etapeRetraitVendeur) {
		this.etapeRetraitVendeur = etapeRetraitVendeur;
	}

	public String getEtapeDepotClient() {
		return etapeDepotClient;
	}

	public void setEtapeDepotClient(String etapeDepotClient) {
		this.etapeDepotClient = etapeDepotClient;
	}
}
