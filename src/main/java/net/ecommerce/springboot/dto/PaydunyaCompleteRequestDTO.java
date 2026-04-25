package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public class PaydunyaCompleteRequestDTO {

	@NotBlank
	private String invoiceToken;

	public String getInvoiceToken() {
		return invoiceToken;
	}

	public void setInvoiceToken(String invoiceToken) {
		this.invoiceToken = invoiceToken;
	}
}
