package net.ecommerce.springboot.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.CartAddFromNegotiationDTO;
import net.ecommerce.springboot.dto.CartAddItemDTO;
import net.ecommerce.springboot.dto.CartDTO;
import net.ecommerce.springboot.dto.CartPatchItemDTO;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService cartService;
	private final AuthService authService;

	public CartController(CartService cartService, AuthService authService) {
		this.cartService = cartService;
		this.authService = authService;
	}

	@GetMapping
	public ResponseEntity<?> getCart() {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(cartService.getCartDto(u));
	}

	@PostMapping("/items")
	public ResponseEntity<?> addItem(@Valid @RequestBody CartAddItemDTO body) {
		User u = requireUser();
		try {
			CartDTO dto = cartService.addCatalogueLine(u, body.getIdArticle(), body.getQuantity());
			return ResponseEntity.status(HttpStatus.CREATED).body(dto);
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/items/from-negotiation")
	public ResponseEntity<?> addFromNegotiation(@Valid @RequestBody CartAddFromNegotiationDTO body) {
		User u = requireUser();
		try {
			CartDTO dto = cartService.addFromNegotiation(u, body.getConversationId(), body.getMessageId());
			return ResponseEntity.status(HttpStatus.CREATED).body(dto);
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/items/{id}")
	public ResponseEntity<?> patchItem(@PathVariable("id") Integer id, @Valid @RequestBody CartPatchItemDTO body) {
		User u = requireUser();
		try {
			return ResponseEntity.ok(cartService.updateLineQuantity(u, id, body.getQuantity()));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping("/items/{id}")
	public ResponseEntity<?> removeItem(@PathVariable("id") Integer id) {
		User u = requireUser();
		try {
			return ResponseEntity.ok(cartService.removeLine(u, id));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping
	public ResponseEntity<?> clear() {
		User u = requireUser();
		cartService.clear(u);
		return ResponseEntity.ok(cartService.getCartDto(u));
	}

	private User requireUser() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		return u;
	}
}
