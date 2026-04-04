package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public class LivreurTakeDeliveryDTO {

	@NotBlank(message = "typeEngin requis : MOTO ou VEHICULE")
	private String typeEngin;

	public String getTypeEngin() {
		return typeEngin;
	}

	public void setTypeEngin(String typeEngin) {
		this.typeEngin = typeEngin;
	}
}
