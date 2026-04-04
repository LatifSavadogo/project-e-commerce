package net.ecommerce.springboot.model;

public enum OfferStatus {
	/** Proposition de prix acheteur en attente de réponse vendeur. */
	PENDING,
	/** Proposition acheteur acceptée par le vendeur (paiement à ce montant). */
	ACCEPTED,
	/** Proposition acheteur refusée (tour 1 uniquement via l’API refus). */
	REFUSED,
	/** Prix final vendeur en attente de validation / refus acheteur. */
	PENDING_BUYER_FINAL,
	/** Acheteur a validé le prix final (paiement autorisé à ce montant). */
	VALIDATED,
	/** Acheteur a refusé le prix final (fin de la négociation sur ce fil). */
	EXPIRED
}
