package net.ecommerce.springboot.dto;

import java.util.ArrayList;
import java.util.List;

public class LivreurDashboardDTO {

	private long livraisonsEnCours;
	private long livraisonsLivrees;
	private long livraisonsLivreesMoto;
	private long livraisonsLivreesVehicule;
	private String enginProfil;
	private List<LivraisonDTO> dernieresCourses = new ArrayList<>();

	public long getLivraisonsEnCours() {
		return livraisonsEnCours;
	}

	public void setLivraisonsEnCours(long livraisonsEnCours) {
		this.livraisonsEnCours = livraisonsEnCours;
	}

	public long getLivraisonsLivrees() {
		return livraisonsLivrees;
	}

	public void setLivraisonsLivrees(long livraisonsLivrees) {
		this.livraisonsLivrees = livraisonsLivrees;
	}

	public long getLivraisonsLivreesMoto() {
		return livraisonsLivreesMoto;
	}

	public void setLivraisonsLivreesMoto(long livraisonsLivreesMoto) {
		this.livraisonsLivreesMoto = livraisonsLivreesMoto;
	}

	public long getLivraisonsLivreesVehicule() {
		return livraisonsLivreesVehicule;
	}

	public void setLivraisonsLivreesVehicule(long livraisonsLivreesVehicule) {
		this.livraisonsLivreesVehicule = livraisonsLivreesVehicule;
	}

	public String getEnginProfil() {
		return enginProfil;
	}

	public void setEnginProfil(String enginProfil) {
		this.enginProfil = enginProfil;
	}

	public List<LivraisonDTO> getDernieresCourses() {
		return dernieresCourses;
	}

	public void setDernieresCourses(List<LivraisonDTO> dernieresCourses) {
		this.dernieresCourses = dernieresCourses;
	}
}
