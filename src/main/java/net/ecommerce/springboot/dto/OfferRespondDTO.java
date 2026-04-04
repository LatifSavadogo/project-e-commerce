package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotNull;
import net.ecommerce.springboot.model.OfferStatus;

public class OfferRespondDTO {

	@NotNull
	private OfferStatus statut;

	public OfferStatus getStatut() {
		return statut;
	}

	public void setStatut(OfferStatus statut) {
		this.statut = statut;
	}
}
