package net.ecommerce.springboot.model;

public enum PaymentMethod {
	ORANGE_MONEY,
	MOOV_MONEY,
	VIREMENT,
	ESPECES,
	/** Paiement en ligne via PayDunya (Orange, Moov, carte… selon configuration PayDunya). */
	PAYDUNYA
}
