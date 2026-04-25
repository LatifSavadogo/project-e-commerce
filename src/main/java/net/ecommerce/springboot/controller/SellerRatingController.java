package net.ecommerce.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.SellerRatingCreateDTO;
import net.ecommerce.springboot.model.SellerRating;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.SellerRatingService;

@RestController
@RequestMapping("/api/v1/payments")
public class SellerRatingController {

	private final SellerRatingService sellerRatingService;
	private final AuthService authService;

	public SellerRatingController(SellerRatingService sellerRatingService, AuthService authService) {
		this.sellerRatingService = sellerRatingService;
		this.authService = authService;
	}

	@PostMapping("/{id}/rating")
	public ResponseEntity<java.util.Map<String, Object>> noterVendeur(@PathVariable("id") Integer idtransaction,
			@Valid @RequestBody SellerRatingCreateDTO body) {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		SellerRating r = sellerRatingService.createRatingFromBuyer(u, idtransaction, body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(java.util.Map.of("id", r.getId(), "stars", r.getStars(), "idtransaction", idtransaction));
	}
}
