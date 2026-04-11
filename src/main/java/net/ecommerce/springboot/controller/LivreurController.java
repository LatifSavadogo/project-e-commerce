package net.ecommerce.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.LivraisonLivreurDTO;
import net.ecommerce.springboot.dto.LivraisonPositionDTO;
import net.ecommerce.springboot.dto.LivraisonTerminerRequestDTO;
import net.ecommerce.springboot.dto.LivreurDashboardDTO;
import net.ecommerce.springboot.dto.LivreurMesLivraisonsDTO;
import net.ecommerce.springboot.dto.LivreurEnginPatchDTO;
import net.ecommerce.springboot.dto.LivreurTakeDeliveryDTO;
import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.LivraisonService;

@RestController
@RequestMapping("/api/v1/livreur")
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
		User u = requireLivreur();
		return ResponseEntity.ok(livraisonService.listDisponiblesPourLivreur(u));
	}

	/** En cours + historique (livrées / annulées) avec détails pour la console livreur. */
	@GetMapping("/livraisons/mes-livraisons")
	public ResponseEntity<LivreurMesLivraisonsDTO> mesLivraisons() {
		User u = requireLivreur();
		return ResponseEntity.ok(livraisonService.listMesLivraisonsPartitionnees(u));
	}

	@PostMapping("/livraisons/{id}/ignorer")
	public ResponseEntity<?> ignorer(@PathVariable("id") Integer id) {
		User u = requireLivreur();
		try {
			livraisonService.ignorerOffre(u, id);
			return ResponseEntity.ok(java.util.Map.of("ok", true));
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(404).body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
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
			LivraisonLivreurDTO dto = livraisonService.prendreEnCharge(u, id, engin);
			return ResponseEntity.ok(dto);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(404).body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/livraisons/{id}/position")
	public ResponseEntity<?> publierPosition(@PathVariable("id") Integer id, @Valid @RequestBody LivraisonPositionDTO body) {
		User u = requireLivreur();
		try {
			livraisonService.publierPositionLivreur(u, id, body);
			return ResponseEntity.ok(java.util.Map.of("ok", true));
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(404).body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	/** Finalisation après scan du QR affiché par le client (obligatoire). */
	@PostMapping("/livraisons/terminer-par-scan")
	public ResponseEntity<?> terminerParScan(@Valid @RequestBody LivraisonTerminerRequestDTO body) {
		User u = requireLivreur();
		try {
			return ResponseEntity.ok(livraisonService.terminerParScanQrClient(u, body.getClientQrPayload()));
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(404).body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
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

	/**
	 * Contrôle le rôle livreur à partir de l’utilisateur rechargé en base (via la session), pas à partir des
	 * {@code GrantedAuthority} figés au login — sinon, après validation admin d’une demande livreur, la session
	 * resterait en ROLE_ACHETEUR et les API livreur renverraient 403 alors que /me affiche déjà LIVREUR.
	 */
	private User requireLivreur() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		if (u.getRole() == null || !RoleNames.LIVREUR.equalsIgnoreCase(u.getRole().getLibrole())) {
			throw new AccessDeniedException("Réservé aux comptes livreur.");
		}
		return u;
	}
}
