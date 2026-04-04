package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

import net.ecommerce.springboot.model.Complaint;

public class ComplaintDTO {

	private Integer idplainte;
	private Integer idAuteur;
	private String auteurNom;
	private String auteurEmail;
	private Integer idArticle;
	private String titre;
	private String description;
	private boolean lu;
	private LocalDateTime datecreation;

	public static ComplaintDTO fromEntity(Complaint c) {
		ComplaintDTO d = new ComplaintDTO();
		d.setIdplainte(c.getIdplainte());
		if (c.getAuteur() != null) {
			d.setIdAuteur(c.getAuteur().getIduser());
			d.setAuteurNom(c.getAuteur().getNom() + " " + c.getAuteur().getPrenom());
			d.setAuteurEmail(c.getAuteur().getEmail());
		}
		if (c.getArticle() != null) {
			d.setIdArticle(c.getArticle().getIdarticle());
		}
		d.setTitre(c.getTitre());
		d.setDescription(c.getDescription());
		d.setLu(c.isLu());
		d.setDatecreation(c.getDatecreation());
		return d;
	}

	public Integer getIdplainte() {
		return idplainte;
	}

	public void setIdplainte(Integer idplainte) {
		this.idplainte = idplainte;
	}

	public Integer getIdAuteur() {
		return idAuteur;
	}

	public void setIdAuteur(Integer idAuteur) {
		this.idAuteur = idAuteur;
	}

	public String getAuteurNom() {
		return auteurNom;
	}

	public void setAuteurNom(String auteurNom) {
		this.auteurNom = auteurNom;
	}

	public String getAuteurEmail() {
		return auteurEmail;
	}

	public void setAuteurEmail(String auteurEmail) {
		this.auteurEmail = auteurEmail;
	}

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isLu() {
		return lu;
	}

	public void setLu(boolean lu) {
		this.lu = lu;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}
}
