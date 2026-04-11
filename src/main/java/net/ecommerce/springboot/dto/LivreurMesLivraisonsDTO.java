package net.ecommerce.springboot.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue consolidée pour la console livreur : courses actives et historique (livrées / annulées).
 */
public class LivreurMesLivraisonsDTO {

	private List<LivraisonLivreurDTO> enCours = new ArrayList<>();
	private List<LivraisonLivreurDTO> terminees = new ArrayList<>();
	/** Nombre max d’entrées lues en base (pagination côté serveur). */
	private int limiteChargee;

	public List<LivraisonLivreurDTO> getEnCours() {
		return enCours;
	}

	public void setEnCours(List<LivraisonLivreurDTO> enCours) {
		this.enCours = enCours;
	}

	public List<LivraisonLivreurDTO> getTerminees() {
		return terminees;
	}

	public void setTerminees(List<LivraisonLivreurDTO> terminees) {
		this.terminees = terminees;
	}

	public int getLimiteChargee() {
		return limiteChargee;
	}

	public void setLimiteChargee(int limiteChargee) {
		this.limiteChargee = limiteChargee;
	}
}
