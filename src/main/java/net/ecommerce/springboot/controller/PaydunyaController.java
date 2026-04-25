package net.ecommerce.springboot.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.config.PaydunyaProperties;
import net.ecommerce.springboot.dto.PaydunyaCheckoutResponseDTO;
import net.ecommerce.springboot.dto.PaydunyaCompleteRequestDTO;
import net.ecommerce.springboot.dto.PaydunyaCompleteResponseDTO;
import net.ecommerce.springboot.dto.PaydunyaOrderCheckoutDTO;
import net.ecommerce.springboot.dto.VendorCertificationCheckoutDTO;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.PaydunyaService;

@RestController
@RequestMapping("/api/v1/paydunya")
public class PaydunyaController {

	private final PaydunyaService paydunyaService;
	private final AuthService authService;
	private final PaydunyaProperties paydunyaProperties;

	public PaydunyaController(PaydunyaService paydunyaService, AuthService authService,
			PaydunyaProperties paydunyaProperties) {
		this.paydunyaService = paydunyaService;
		this.authService = authService;
		this.paydunyaProperties = paydunyaProperties;
	}

	@PostMapping("/invoices/order")
	public ResponseEntity<PaydunyaCheckoutResponseDTO> invoiceOrder(@Valid @RequestBody PaydunyaOrderCheckoutDTO body) {
		User u = requireUser();
		return ResponseEntity.ok(paydunyaService.createOrderCheckout(u, body));
	}

	@PostMapping("/invoices/certification")
	public ResponseEntity<PaydunyaCheckoutResponseDTO> invoiceCert(@Valid @RequestBody VendorCertificationCheckoutDTO body) {
		User u = requireUser();
		return ResponseEntity.ok(paydunyaService.createCertificationCheckout(u, body));
	}

	@PostMapping("/complete")
	public ResponseEntity<PaydunyaCompleteResponseDTO> complete(@Valid @RequestBody PaydunyaCompleteRequestDTO body) {
		User u = requireUser();
		return ResponseEntity.ok(paydunyaService.completeInvoice(body.getInvoiceToken().trim(), u));
	}

	/**
	 * IPN PayDunya (form-data {@code data=} JSON ou JSON brut).
	 */
	@PostMapping(value = "/ipn", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE, MediaType.ALL_VALUE })
	public ResponseEntity<String> ipn(
			@RequestParam(value = "data", required = false) String data,
			@RequestBody(required = false) String rawBody,
			@RequestHeader(value = "PAYDUNYA-MASTER-KEY", required = false) String ipnMasterKey) {
		paydunyaService.assertConfigured();
		if (ipnMasterKey != null && !ipnMasterKey.isBlank()
				&& !ipnMasterKey.equals(paydunyaProperties.getMasterKey())) {
			return ResponseEntity.status(401).body("invalid master key");
		}
		String payload = (data != null && !data.isBlank()) ? data : rawBody;
		if (payload == null || payload.isBlank()) {
			return ResponseEntity.badRequest().body("missing payload");
		}
		paydunyaService.settleFromIpnJson(payload);
		return ResponseEntity.ok("OK");
	}

	private User requireUser() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		return u;
	}
}
