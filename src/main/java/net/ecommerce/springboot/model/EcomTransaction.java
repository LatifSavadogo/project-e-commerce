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
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "EcomTransaction")
public class EcomTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idtransaction;

	@ManyToOne
	@JoinColumn(name = "iduser_acheteur", nullable = false)
	private User acheteur;

	@ManyToOne
	@JoinColumn(name = "iduser_vendeur", nullable = false)
	private User vendeur;

	@ManyToOne
	@JoinColumn(name = "idarticle", nullable = false)
	private Article article;

	@Column(nullable = false)
	private int quantite;

	@Column(nullable = false)
	private int prixUnitaireSnapshot;

	@Column(nullable = false)
	private int montantTotal;

	/** Frais affichés au moment du paiement (0 % mobile money selon spec). */
	@Column(nullable = false)
	private int fraisAffiches;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PaymentMethod moyenPaiement;

	/** SHA-256 hex (normalisation référence) pour unicité sans stocker la référence en clair. */
	@Column(nullable = false, length = 64, unique = true)
	private String refExterneHash;

	/** Référence opérateur / banque chiffrée AES-256-GCM (Base64). */
	@Column(nullable = false, length = 2048)
	private String refExterneCryptee;

	@Column(nullable = false)
	private LocalDateTime datecreation;

	@OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
	private Livraison livraison;

	@PrePersist
	public void prePersist() {
		datecreation = LocalDateTime.now();
	}

	public EcomTransaction() {
	}

	public Integer getIdtransaction() {
		return idtransaction;
	}

	public void setIdtransaction(Integer idtransaction) {
		this.idtransaction = idtransaction;
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

	public int getQuantite() {
		return quantite;
	}

	public void setQuantite(int quantite) {
		this.quantite = quantite;
	}

	public int getPrixUnitaireSnapshot() {
		return prixUnitaireSnapshot;
	}

	public void setPrixUnitaireSnapshot(int prixUnitaireSnapshot) {
		this.prixUnitaireSnapshot = prixUnitaireSnapshot;
	}

	public int getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(int montantTotal) {
		this.montantTotal = montantTotal;
	}

	public int getFraisAffiches() {
		return fraisAffiches;
	}

	public void setFraisAffiches(int fraisAffiches) {
		this.fraisAffiches = fraisAffiches;
	}

	public PaymentMethod getMoyenPaiement() {
		return moyenPaiement;
	}

	public void setMoyenPaiement(PaymentMethod moyenPaiement) {
		this.moyenPaiement = moyenPaiement;
	}

	public String getRefExterneHash() {
		return refExterneHash;
	}

	public void setRefExterneHash(String refExterneHash) {
		this.refExterneHash = refExterneHash;
	}

	public String getRefExterneCryptee() {
		return refExterneCryptee;
	}

	public void setRefExterneCryptee(String refExterneCryptee) {
		this.refExterneCryptee = refExterneCryptee;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}

	public Livraison getLivraison() {
		return livraison;
	}

	public void setLivraison(Livraison livraison) {
		this.livraison = livraison;
	}
}
