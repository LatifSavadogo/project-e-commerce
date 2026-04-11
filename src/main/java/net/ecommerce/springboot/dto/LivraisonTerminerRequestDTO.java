package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public class LivraisonTerminerRequestDTO {

	@NotBlank
	private String clientQrPayload;

	public String getClientQrPayload() {
		return clientQrPayload;
	}

	public void setClientQrPayload(String clientQrPayload) {
		this.clientQrPayload = clientQrPayload;
	}
}
