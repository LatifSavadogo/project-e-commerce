package net.ecommerce.springboot.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.LivraisonPositionDTO;
import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.UserService;

/**
 * Coordonnées du **domicile** (référence obligatoire). Au paiement, un autre point GPS peut être indiqué pour la
 * livraison de la commande.
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeDeliveryLocationController {

	private final AuthService authService;
	private final UserService userService;

	public MeDeliveryLocationController(AuthService authService, UserService userService) {
		this.authService = authService;
		this.userService = userService;
	}

	@RequestMapping(value = "/delivery-location", method = { RequestMethod.PATCH, RequestMethod.POST,
			RequestMethod.PUT }, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> saveDomicileGps(@Valid @RequestBody LivraisonPositionDTO body) {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
		}
		try {
			User updated = userService.updateMyDeliveryLocation(u, body.getLatitude(), body.getLongitude());
			return ResponseEntity.ok(UserDTO.fromEntity(updated));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}
}
