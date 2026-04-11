package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Livraison")
public class Livraison {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idlivraison;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "idtransaction", nullable = false, unique = true)
	private EcomTransaction transaction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idlivreur", nullable = true)
	private User livreur;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private LivraisonStatut statut;

	/** Engin utilisé pour cette course (choisi à la prise en charge). */
	@Enumerated(EnumType.STRING)
	@Column(name = "type_engin_utilise", length = 16)
	private TypeEnginLivreur typeEnginUtilise;

	@Column(nullable = false)
	private LocalDateTime datecreation;

	private LocalDateTime datePriseEnCharge;

	private LocalDateTime dateLivraison;

	/**
	 * Jeton secret contenu dans le QR code client ; le livreur le scanne pour terminer la livraison.
	 */
	@Column(name = "client_delivery_token", length = 80)
	private String clientDeliveryToken;

	/** Code à communiquer au vendeur pour préparer le colis (affiché côté vendeur uniquement). */
	@Column(name = "vendor_pickup_code", length = 16, unique = true)
	private String vendorPickupCode;

	/** Dernière position GPS partagée par le livreur pour le suivi client (Maps). */
	@Column(name = "livreur_last_latitude")
	private Double livreurLastLatitude;

	@Column(name = "livreur_last_longitude")
	private Double livreurLastLongitude;

	@Column(name = "livreur_position_at")
	private LocalDateTime livreurPositionAt;

	@PrePersist
	public void prePersist() {
		if (datecreation == null) {
			datecreation = LocalDateTime.now();
		}
	}

	public Integer getIdlivraison() {
		return idlivraison;
	}

	public void setIdlivraison(Integer idlivraison) {
		this.idlivraison = idlivraison;
	}

	public EcomTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(EcomTransaction transaction) {
		this.transaction = transaction;
	}

	public User getLivreur() {
		return livreur;
	}

	public void setLivreur(User livreur) {
		this.livreur = livreur;
	}

	public LivraisonStatut getStatut() {
		return statut;
	}

	public void setStatut(LivraisonStatut statut) {
		this.statut = statut;
	}

	public TypeEnginLivreur getTypeEnginUtilise() {
		return typeEnginUtilise;
	}

	public void setTypeEnginUtilise(TypeEnginLivreur typeEnginUtilise) {
		this.typeEnginUtilise = typeEnginUtilise;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}

	public LocalDateTime getDatePriseEnCharge() {
		return datePriseEnCharge;
	}

	public void setDatePriseEnCharge(LocalDateTime datePriseEnCharge) {
		this.datePriseEnCharge = datePriseEnCharge;
	}

	public LocalDateTime getDateLivraison() {
		return dateLivraison;
	}

	public void setDateLivraison(LocalDateTime dateLivraison) {
		this.dateLivraison = dateLivraison;
	}

	public String getClientDeliveryToken() {
		return clientDeliveryToken;
	}

	public void setClientDeliveryToken(String clientDeliveryToken) {
		this.clientDeliveryToken = clientDeliveryToken;
	}

	public String getVendorPickupCode() {
		return vendorPickupCode;
	}

	public void setVendorPickupCode(String vendorPickupCode) {
		this.vendorPickupCode = vendorPickupCode;
	}

	/** Contenu à encoder dans le QR (sans montants). */
	public String buildClientQrPayload() {
		if (idlivraison == null || clientDeliveryToken == null || clientDeliveryToken.isBlank()) {
			return null;
		}
		return "ECOM;" + idlivraison + ";" + clientDeliveryToken;
	}

	public Double getLivreurLastLatitude() {
		return livreurLastLatitude;
	}

	public void setLivreurLastLatitude(Double livreurLastLatitude) {
		this.livreurLastLatitude = livreurLastLatitude;
	}

	public Double getLivreurLastLongitude() {
		return livreurLastLongitude;
	}

	public void setLivreurLastLongitude(Double livreurLastLongitude) {
		this.livreurLastLongitude = livreurLastLongitude;
	}

	public LocalDateTime getLivreurPositionAt() {
		return livreurPositionAt;
	}

	public void setLivreurPositionAt(LocalDateTime livreurPositionAt) {
		this.livreurPositionAt = livreurPositionAt;
	}
}
