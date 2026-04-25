package net.ecommerce.springboot.dto;

public class PaydunyaCheckoutResponseDTO {

	private String checkoutUrl;
	private String invoiceToken;
	private String description;

	public String getCheckoutUrl() {
		return checkoutUrl;
	}

	public void setCheckoutUrl(String checkoutUrl) {
		this.checkoutUrl = checkoutUrl;
	}

	public String getInvoiceToken() {
		return invoiceToken;
	}

	public void setInvoiceToken(String invoiceToken) {
		this.invoiceToken = invoiceToken;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
