package net.ecommerce.springboot.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.ecommerce.springboot.dto.RoleUpgradeRequestDTO;
import net.ecommerce.springboot.model.RoleUpgradeRequest;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.RoleUpgradeService;

@RestController
@RequestMapping("/api/v1/me/role-upgrade")
public class MeRoleUpgradeController {

	private final AuthService authService;
	private final RoleUpgradeService roleUpgradeService;

	public MeRoleUpgradeController(AuthService authService, RoleUpgradeService roleUpgradeService) {
		this.authService = authService;
		this.roleUpgradeService = roleUpgradeService;
	}

	@GetMapping
	public ResponseEntity<?> derniereDemande() {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
		}
		Map<String, Object> body = new HashMap<>();
		body.put("demande", roleUpgradeService.findLatestForUser(u.getIduser())
				.map(RoleUpgradeRequestDTO::fromEntity)
				.orElse(null));
		return ResponseEntity.ok(body);
	}

	@PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> soumettre(@RequestParam String roleDemande,
			@RequestParam("cnib") MultipartFile cnib,
			@RequestParam("photo") MultipartFile photo,
			@RequestParam(required = false) String latitude,
			@RequestParam(required = false) String longitude,
			@RequestParam(required = false) Integer idtypeVendeur,
			@RequestParam(required = false) String typeEnginLivreur) {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
		}
		try {
			Double lat = parseCoord(latitude);
			Double lon = parseCoord(longitude);
			RoleUpgradeRequest saved = roleUpgradeService.submit(u, roleDemande, cnib, photo, lat, lon, idtypeVendeur,
					typeEnginLivreur);
			return ResponseEntity.status(201).body(RoleUpgradeRequestDTO.fromEntity(saved));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (IOException ex) {
			return ResponseEntity.internalServerError().body(Map.of("error", "Erreur d'enregistrement des fichiers."));
		}
	}

	private static Double parseCoord(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return Double.parseDouble(raw.trim().replace(',', '.'));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Coordonnée GPS invalide.");
		}
	}
}
