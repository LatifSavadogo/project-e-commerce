package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ChatMessage")
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idmessage;

	@ManyToOne
	@JoinColumn(name = "idconversation", nullable = false)
	private Conversation conversation;

	@ManyToOne
	@JoinColumn(name = "iduser_auteur", nullable = false)
	private User auteur;

	@Column(nullable = false, length = 8000)
	private String contenu;

	@Column(nullable = false)
	private LocalDateTime dateenvoi;

	/** Non null = message avec offre de prix ; statut géré par le vendeur. */
	private Integer prixPropose;

	/**
	 * Quantité visée par l’offre (unités). Obligatoire pour les nouvelles propositions acheteur avec prix ;
	 * renseignée sur le prix final vendeur. Anciennes lignes : null traité comme 1 pour compatibilité.
	 */
	@Column(name = "quantite_proposee")
	private Integer quantiteProposee;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private OfferStatus statutOffre;

	/** Dernier prix imposé par le vendeur après 2 refus (acheteur peut payer ou renoncer). */
	@Column(name = "offre_finale_vendeur")
	private Boolean offreFinaleVendeur;

	@PrePersist
	public void prePersist() {
		dateenvoi = LocalDateTime.now();
	}

	public ChatMessage() {
	}

	public Integer getIdmessage() {
		return idmessage;
	}

	public void setIdmessage(Integer idmessage) {
		this.idmessage = idmessage;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public User getAuteur() {
		return auteur;
	}

	public void setAuteur(User auteur) {
		this.auteur = auteur;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public LocalDateTime getDateenvoi() {
		return dateenvoi;
	}

	public void setDateenvoi(LocalDateTime dateenvoi) {
		this.dateenvoi = dateenvoi;
	}

	public Integer getPrixPropose() {
		return prixPropose;
	}

	public void setPrixPropose(Integer prixPropose) {
		this.prixPropose = prixPropose;
	}

	public Integer getQuantiteProposee() {
		return quantiteProposee;
	}

	public void setQuantiteProposee(Integer quantiteProposee) {
		this.quantiteProposee = quantiteProposee;
	}

	public OfferStatus getStatutOffre() {
		return statutOffre;
	}

	public void setStatutOffre(OfferStatus statutOffre) {
		this.statutOffre = statutOffre;
	}

	public Boolean getOffreFinaleVendeur() {
		return offreFinaleVendeur;
	}

	public void setOffreFinaleVendeur(Boolean offreFinaleVendeur) {
		this.offreFinaleVendeur = offreFinaleVendeur;
	}
}
