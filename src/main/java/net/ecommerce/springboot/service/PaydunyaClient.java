package net.ecommerce.springboot.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.ecommerce.springboot.config.PaydunyaProperties;

@Component
public class PaydunyaClient {

	private final ObjectMapper objectMapper;
	private final RestClient restClient = RestClient.create();

	public PaydunyaClient(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public JsonNode postCheckoutInvoice(PaydunyaProperties props, ObjectNode body) {
		String raw = restClient.post()
				.uri(props.checkoutInvoiceCreateUrl())
				.headers(h -> {
					h.set("PAYDUNYA-MASTER-KEY", props.getMasterKey());
					h.set("PAYDUNYA-PRIVATE-KEY", props.getPrivateKey());
					h.set("PAYDUNYA-TOKEN", props.getToken());
					h.setContentType(MediaType.APPLICATION_JSON);
				})
				.body(body)
				.retrieve()
				.body(String.class);
		try {
			return objectMapper.readTree(raw);
		} catch (Exception e) {
			throw new IllegalStateException("Réponse PayDunya invalide (création facture).", e);
		}
	}

	public JsonNode getCheckoutInvoiceConfirm(PaydunyaProperties props, String invoiceToken) {
		String url = props.checkoutInvoiceConfirmUrl(invoiceToken);
		String raw = restClient.get()
				.uri(url)
				.headers(h -> {
					h.set("PAYDUNYA-MASTER-KEY", props.getMasterKey());
					h.set("PAYDUNYA-PRIVATE-KEY", props.getPrivateKey());
					h.set("PAYDUNYA-TOKEN", props.getToken());
					h.setContentType(MediaType.APPLICATION_JSON);
				})
				.retrieve()
				.body(String.class);
		try {
			return objectMapper.readTree(raw);
		} catch (Exception e) {
			throw new IllegalStateException("Réponse PayDunya invalide (confirmation facture).", e);
		}
	}
}
