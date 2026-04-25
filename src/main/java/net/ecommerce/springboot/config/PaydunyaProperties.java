package net.ecommerce.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.paydunya")
public class PaydunyaProperties {

	private String masterKey = "";
	private String privateKey = "";
	private String token = "";
	private boolean sandbox = true;
	private String storeName = "Ecomarket";
	/** Ex. http://localhost:5173/paiement/paydunya (PayDunya ajoute ?token=…) */
	private String frontendReturnBaseUrl = "http://localhost:5173/paiement/paydunya";
	/** URL publique du backend pour callback IPN, ex. http://localhost:8080 */
	private String backendPublicBaseUrl = "http://localhost:8080";

	public String getMasterKey() {
		return masterKey;
	}

	public void setMasterKey(String masterKey) {
		this.masterKey = masterKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getFrontendReturnBaseUrl() {
		return frontendReturnBaseUrl;
	}

	public void setFrontendReturnBaseUrl(String frontendReturnBaseUrl) {
		this.frontendReturnBaseUrl = frontendReturnBaseUrl;
	}

	public String getBackendPublicBaseUrl() {
		return backendPublicBaseUrl;
	}

	public void setBackendPublicBaseUrl(String backendPublicBaseUrl) {
		this.backendPublicBaseUrl = backendPublicBaseUrl;
	}

	public boolean isConfigured() {
		return masterKey != null && !masterKey.isBlank()
				&& privateKey != null && !privateKey.isBlank()
				&& token != null && !token.isBlank();
	}

	public String checkoutInvoiceCreateUrl() {
		return sandbox
				? "https://app.paydunya.com/sandbox-api/v1/checkout-invoice/create"
				: "https://app.paydunya.com/api/v1/checkout-invoice/create";
	}

	public String checkoutInvoiceConfirmUrl(String invoiceToken) {
		String base = sandbox
				? "https://app.paydunya.com/sandbox-api/v1/checkout-invoice/confirm/"
				: "https://app.paydunya.com/api/v1/checkout-invoice/confirm/";
		return base + invoiceToken;
	}

	public String callbackUrl() {
		return trimSlash(backendPublicBaseUrl) + "/api/v1/paydunya/ipn";
	}
	private static String trimSlash(String u) {
		if (u == null || u.isBlank()) {
			return "";
		}
		return u.endsWith("/") ? u.substring(0, u.length() - 1) : u;
	}
}
