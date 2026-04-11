package net.ecommerce.springboot.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.RoleUpgradeRejectDTO;
import net.ecommerce.springboot.dto.RoleUpgradeRequestDTO;
import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.model.RoleUpgradeRequest;
import net.ecommerce.springboot.model.RoleUpgradeStatus;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.RoleUpgradeRequestRepository;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.RoleUpgradeService;

@RestController
@RequestMapping("/api/v1/admin/role-upgrades")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminRoleUpgradeController {

	private final RoleUpgradeService roleUpgradeService;
	private final RoleUpgradeRequestRepository requestRepository;
	private final AuthService authService;

	public AdminRoleUpgradeController(RoleUpgradeService roleUpgradeService,
			RoleUpgradeRequestRepository requestRepository, AuthService authService) {
		this.roleUpgradeService = roleUpgradeService;
		this.requestRepository = requestRepository;
		this.authService = authService;
	}

	@GetMapping
	public ResponseEntity<List<RoleUpgradeRequestDTO>> lister(
			@RequestParam(required = false) RoleUpgradeStatus status) {
		List<RoleUpgradeRequestDTO> list = roleUpgradeService.listForAdmin(status).stream()
				.map(RoleUpgradeRequestDTO::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(list);
	}

	@PostMapping("/{id}/approve")
	public ResponseEntity<?> approuver(@PathVariable Integer id) {
		User admin = authService.getCurrentUser();
		try {
			User updated = roleUpgradeService.approve(id, admin);
			return ResponseEntity.ok(UserDTO.fromEntity(updated));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@PostMapping("/{id}/reject")
	public ResponseEntity<?> refuser(@PathVariable Integer id, @Valid @RequestBody(required = false) RoleUpgradeRejectDTO body) {
		User admin = authService.getCurrentUser();
		String motif = body != null && body.getMotif() != null ? body.getMotif().trim() : null;
		try {
			roleUpgradeService.reject(id, admin, motif);
			return ResponseEntity.ok(Map.of("ok", true));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping("/{id}/fichiers/cnib")
	public ResponseEntity<Resource> telechargerCnib(@PathVariable Integer id) {
		return fichierPending(id, true);
	}

	@GetMapping("/{id}/fichiers/photo")
	public ResponseEntity<Resource> telechargerPhoto(@PathVariable Integer id) {
		return fichierPending(id, false);
	}

	private ResponseEntity<Resource> fichierPending(Integer id, boolean cnib) {
		RoleUpgradeRequest req = requestRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
		if (req.getStatus() != RoleUpgradeStatus.PENDING) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.GONE,
					"Fichiers non disponibles (demande déjà traitée).");
		}
		String name = cnib ? req.getCnibFilename() : req.getPhotoFilename();
		try {
			Path path = Paths.get(RoleUpgradeService.PENDING_DIR, name).normalize();
			Resource resource = new UrlResource(path.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				return ResponseEntity.notFound().build();
			}
			String contentType = Files.probeContentType(path);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
					.body(resource);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
