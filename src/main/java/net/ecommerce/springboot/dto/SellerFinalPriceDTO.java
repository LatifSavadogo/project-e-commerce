package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SellerFinalPriceDTO {

	@NotNull
	@Min(1)
	private Integer prix;

	public Integer getPrix() {
		return prix;
	}

	public void setPrix(Integer prix) {
		this.prix = prix;
	}
}
