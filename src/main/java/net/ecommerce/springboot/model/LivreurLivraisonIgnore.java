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
import jakarta.persistence.UniqueConstraint;

/**
 * Une livreur masque une offre de sa liste « disponibles » sans affecter les autres livreurs.
 */
@Entity
@Table(name = "LivreurLivraisonIgnore", uniqueConstraints = @UniqueConstraint(columnNames = { "idlivreur",
		"idlivraison" }))
public class LivreurLivraisonIgnore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "idlivreur", nullable = false)
	private User livreur;

	@ManyToOne(optional = false)
	@JoinColumn(name = "idlivraison", nullable = false)
	private Livraison livraison;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getLivreur() {
		return livreur;
	}

	public void setLivreur(User livreur) {
		this.livreur = livreur;
	}

	public Livraison getLivraison() {
		return livraison;
	}

	public void setLivraison(Livraison livraison) {
		this.livraison = livraison;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
