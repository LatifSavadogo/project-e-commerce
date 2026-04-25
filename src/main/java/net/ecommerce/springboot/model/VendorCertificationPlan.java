package net.ecommerce.springboot.model;

/** Forfait d’abonnement « vendeur certifié » (montants FCFA). */
public enum VendorCertificationPlan {
	MONTHLY(3000),
	YEARLY(20_000);

	private final int amountFcfa;

	VendorCertificationPlan(int amountFcfa) {
		this.amountFcfa = amountFcfa;
	}

	public int getAmountFcfa() {
		return amountFcfa;
	}
}
