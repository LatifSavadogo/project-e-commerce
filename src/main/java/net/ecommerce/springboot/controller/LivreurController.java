package net.ecommerce.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.LivraisonDTO;
import net.ecommerce.springboot.dto.LivreurDashboardDTO;
import net.ecommerce.springboot.dto.LivreurEnginPatchDTO;
import net.ecommerce.springboot.dto.LivreurTakeDeliveryDTO;
import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.LivraisonService;

@RestController
@RequestMapping("/api/v1/livreur")
@PreAuthorize("hasRole('LIVREUR')")
public class LivreurController {

	private final LivraisonService livraisonService;
	private final AuthService authService;

	public LivreurController(LivraisonService livraisonService, AuthService authService) {
		this.livraisonService = livraisonService;
		this.authService = authService;
	}

	@GetMapping("/dashboard")
	public ResponseEntity<LivreurDashboardDTO> dashboard() {
		User u = requireLivreur();
		return ResponseEntity.ok(livraisonService.buildDashboard(u));
	}

	@GetMapping("/livraisons/disponibles")
	public ResponseEntity<?> disponibles() {
		requireLivreur();
		return ResponseEntity.ok(livraisonService.listDisponibles());
	}

	@PostMapping("/livraisons/{id}/prendre")
	public ResponseEntity<?> prendre(@PathVariable("id") Integer id, @Valid @RequestBody LivreurTakeDeliveryDTO body) {
		User u = requireLivreur();
		final TypeEnginLivreur engin;
		try {
			engin = LivraisonService.parseTypeEngin(body.getTypeEngin());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(java.util.Map.of("error", "typeEngin invalide (MOTO ou VEHICULE)."));
		}
		try {
			LivraisonDTO dto = livraisonService.prendreEnCharge(u, id, engin);
			return ResponseEntity.ok(dto);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/livraisons/{id}/terminer")
	public ResponseEntity<?> terminer(@PathVariable("id") Integer id) {
		User u = requireLivreur();
		try {
			return ResponseEntity.ok(livraisonService.terminerCourse(u, id));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/profil/engin")
	public ResponseEntity<?> patchEngin(@RequestBody LivreurEnginPatchDTO body) {
		User u = requireLivreur();
		try {
			User updated = livraisonService.updateEnginProfil(u, body);
			return ResponseEntity.ok(UserDTO.fromEntity(updated));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	private User requireLivreur() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		return u;
	}
}
