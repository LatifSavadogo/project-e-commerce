package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Plainte")
public class Complaint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idplainte;

	@ManyToOne
	@JoinColumn(name = "iduser", nullable = false)
	private User auteur;

	@ManyToOne
	@JoinColumn(name = "idarticle")
	private Article article;

	@Column(nullable = false, length = 200)
	private String titre;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private boolean lu = false;

	@Column(nullable = false)
	private LocalDateTime datecreation;

	@PrePersist
	public void prePersist() {
		datecreation = LocalDateTime.now();
	}

	public Complaint() {
	}

	public Integer getIdplainte() {
		return idplainte;
	}

	public void setIdplainte(Integer idplainte) {
		this.idplainte = idplainte;
	}

	public User getAuteur() {
		return auteur;
	}

	public void setAuteur(User auteur) {
		this.auteur = auteur;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
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
