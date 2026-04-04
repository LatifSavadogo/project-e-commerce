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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "Conversation")
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idconversation;

	@ManyToOne
	@JoinColumn(name = "idacheteur", nullable = false)
	private User acheteur;

	@ManyToOne
	@JoinColumn(name = "idvendeur", nullable = false)
	private User vendeur;

	@ManyToOne
	@JoinColumn(name = "idarticle")
	private Article article;

	@Column(nullable = false)
	private LocalDateTime datecreation;

	@Column(nullable = false)
	private LocalDateTime dateupdate;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		datecreation = now;
		dateupdate = now;
	}

	@PreUpdate
	public void preUpdate() {
		dateupdate = LocalDateTime.now();
	}

	public Conversation() {
	}

	public Integer getIdconversation() {
		return idconversation;
	}

	public void setIdconversation(Integer idconversation) {
		this.idconversation = idconversation;
	}

	public User getAcheteur() {
		return acheteur;
	}

	public void setAcheteur(User acheteur) {
		this.acheteur = acheteur;
	}

	public User getVendeur() {
		return vendeur;
	}

	public void setVendeur(User vendeur) {
		this.vendeur = vendeur;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}

	public LocalDateTime getDateupdate() {
		return dateupdate;
	}

	public void setDateupdate(LocalDateTime dateupdate) {
		this.dateupdate = dateupdate;
	}
}
