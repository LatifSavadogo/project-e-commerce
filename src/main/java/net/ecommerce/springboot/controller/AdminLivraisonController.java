package net.ecommerce.springboot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ecommerce.springboot.dto.LivraisonDTO;
import net.ecommerce.springboot.service.LivraisonService;

@RestController
@RequestMapping("/api/v1/admin/livraisons")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminLivraisonController {

	private final LivraisonService livraisonService;

	public AdminLivraisonController(LivraisonService livraisonService) {
		this.livraisonService = livraisonService;
	}

	@GetMapping
	public ResponseEntity<List<LivraisonDTO>> listAll() {
		return ResponseEntity.ok(livraisonService.listAllForAdmin());
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		return ResponseEntity.ok(livraisonService.adminStats());
	}
}
