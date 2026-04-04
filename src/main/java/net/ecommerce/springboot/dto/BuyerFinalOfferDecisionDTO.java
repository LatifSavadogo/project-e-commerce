package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotNull;

public class BuyerFinalOfferDecisionDTO {

	/** true = valider le prix final et autoriser le paiement ; false = refus définitif. */
	@NotNull
	private Boolean accept;

	public Boolean getAccept() {
		return accept;
	}

	public void setAccept(Boolean accept) {
		this.accept = accept;
	}
}
