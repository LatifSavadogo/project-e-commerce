package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.CartCheckoutDTO;
import net.ecommerce.springboot.dto.PaymentCreateDTO;
import net.ecommerce.springboot.dto.PaymentResultDTO;
import net.ecommerce.springboot.dto.VendorSalesDashboardDTO;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.CartService;
import net.ecommerce.springboot.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final CartService cartService;
	private final AuthService authService;

	public PaymentController(PaymentService paymentService, CartService cartService, AuthService authService) {
		this.paymentService = paymentService;
		this.cartService = cartService;
		this.authService = authService;
	}

	@PostMapping
	public ResponseEntity<PaymentResultDTO> payer(@Valid @RequestBody PaymentCreateDTO body) {
		User u = requireUser();
		EcomTransaction t = paymentService.enregistrerPaiement(u, body.getIdArticle(), body.getQuantite(),
				body.getMoyenPaiement(), body.getReferenceExterne(), body.getPrixUnitaireNegocie());
		return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResultDTO.fromEntity(t));
	}

	/** Règle chaque ligne du panier comme un paiement distinct (référence externe dérivée par ligne). */
	@PostMapping("/cart-checkout")
	public ResponseEntity<?> payerPanier(@Valid @RequestBody CartCheckoutDTO body) {
		User u = requireUser();
		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(cartService.checkout(u, body.getMoyenPaiement(), body.getReferenceExterne(),
							body.getCartItemIds()));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/mine")
	public ResponseEntity<List<PaymentResultDTO>> mesAchats() {
		User u = requireUser();
		return ResponseEntity.ok(paymentService.listByBuyer(u.getIduser()));
	}

	@GetMapping("/sales")
	public ResponseEntity<List<PaymentResultDTO>> mesVentes() {
		User u = requireUser();
		return ResponseEntity.ok(paymentService.listBySeller(u.getIduser()));
	}

	@GetMapping("/sales/dashboard")
	public ResponseEntity<VendorSalesDashboardDTO> tableauDeBordVentes() {
		User u = requireUser();
		return ResponseEntity.ok(paymentService.buildVendorSalesDashboard(u.getIduser()));
	}

	@GetMapping(value = "/{id}/receipt", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
	public ResponseEntity<String> receipt(@PathVariable Integer id) {
		User u = requireUser();
		EcomTransaction t = paymentService.getTransactionOrThrow(id);
		paymentService.assertCanViewReceipt(t, u);
		String text = paymentService.buildReceiptText(t);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recu-" + id + ".txt\"")
				.body(text);
	}

	private User requireUser() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		return u;
	}
}
