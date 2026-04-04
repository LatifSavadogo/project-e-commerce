package net.ecommerce.springboot.controller;

import net.ecommerce.springboot.dto.FamilleArticleDTO;
import net.ecommerce.springboot.dto.RoleDTO;
import net.ecommerce.springboot.model.FamilleArticle;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.repository.FamilleArticleRepository;
import net.ecommerce.springboot.repository.RoleRepository;
//import net.ecommerce.springboot.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class FamilleArticleController {

	@Autowired
	private FamilleArticleRepository familleArticleRepository;

//    @Autowired
//    private AuthService authService;

	// Lister tous les rôles
	@GetMapping("/familleArticles")
	public List<FamilleArticleDTO> getAllFamilleArticles() {
		return familleArticleRepository.findAll().stream().map(FamilleArticleDTO::fromEntity)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PostMapping("/familleArticles")
	public ResponseEntity<FamilleArticleDTO> createFamilleArticle(
			@Valid @RequestBody FamilleArticleDTO familleArticleDTO) {
//    	String currentUsername = authService.getCurrentUsername();
		FamilleArticle familleArticle = new FamilleArticle();
		familleArticle.setLibfamille(familleArticleDTO.getLibfamille());
		familleArticle.setDescription(familleArticleDTO.getDescription());

		familleArticle.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername); 
		familleArticle.setUserupdate("admin");

		FamilleArticle savedFamilleArticle = familleArticleRepository.save(familleArticle);
		return ResponseEntity.status(HttpStatus.CREATED).body(FamilleArticleDTO.fromEntity(savedFamilleArticle));
	}

	@GetMapping("/familleArticles/{idfamille}")
	public ResponseEntity<FamilleArticleDTO> getFamilleArticleById(@PathVariable Integer idfamille) {
		FamilleArticle familleArticle = familleArticleRepository.findById(idfamille)
				.orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idfamille + " n'existe pas"));
		return ResponseEntity.ok(FamilleArticleDTO.fromEntity(familleArticle));
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping("/familleArticles/{idfamille}")
	public ResponseEntity<FamilleArticleDTO> updateRole(@PathVariable Integer idfamille, @Valid @RequestBody FamilleArticleDTO familleArticleDTO) {
//    	String currentUsername = authService.getCurrentUsername();
		FamilleArticle familleArticle = familleArticleRepository.findById(idfamille)
				.orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idfamille + " n'existe pas"));

		familleArticle.setLibfamille(familleArticleDTO.getLibfamille());
		familleArticle.setDescription(familleArticleDTO.getDescription());

		familleArticle.setDateupdate(LocalDateTime.now());
//        role.setUserupdate(currentUsername);
		familleArticle.setUserupdate("admin");

		FamilleArticle updatedFamilleArticle = familleArticleRepository.save(familleArticle);
		return ResponseEntity.ok(FamilleArticleDTO.fromEntity(updatedFamilleArticle));
	}

	// 5. Supprimer une famille d'Article
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@DeleteMapping("/familleArticles/{idfamille}")
	public ResponseEntity<?> deleteRole(@PathVariable Integer idfamille) {
		FamilleArticle familleArticle = familleArticleRepository.findById(idfamille)
				.orElseThrow(() -> new ResourceNotFoundException("La famille d'Article avec l'id " + idfamille + " n'existe pas"));

		// Vérifier s'il y a des categories article avant suppression
		if (familleArticle.getTypeArticles() != null && !familleArticle.getTypeArticles().isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("supprimé", false);
			errorResponse.put("message",
					"Impossible de supprimer le rôle : il a " + familleArticle.getTypeArticles().size() + " type d'article(s) associé(s)");
			return ResponseEntity.badRequest().body(errorResponse);
		}

		familleArticleRepository.delete(familleArticle);

		Map<String, Boolean> response = new HashMap<>();
		response.put("supprimé", true);
		return ResponseEntity.ok(response);
	}

	// 6. Récupérer les types d'Article d'une famille d'Article
	@GetMapping("/familleArticles/{idfamille}/users")
	public ResponseEntity<?> getUsersByFamilleArticle(@PathVariable Integer idfamille) {
		FamilleArticle familleArticle = familleArticleRepository.findById(idfamille)
				.orElseThrow(() -> new ResourceNotFoundException("Le rôle avec l'id " + idfamille + " n'existe pas"));

		List<Map<String, Object>> typeArticles = familleArticle.getTypeArticles().stream().map(typeArticle -> {
			Map<String, Object> typeArticleMap = new HashMap<>();
			typeArticleMap.put("idtype", typeArticle.getIdtype());
			typeArticleMap.put("desctype", typeArticle.getDesctype());
			typeArticleMap.put("libtype", typeArticle.getLibtype());
			return typeArticleMap;
		}).collect(Collectors.toList());

		Map<String, Object> response = new HashMap<>();
		response.put("Famille Article", FamilleArticleDTO.fromEntity(familleArticle));
		response.put("Type d'article", typeArticles);
		response.put("nombreTypeArticles", typeArticles.size());

		return ResponseEntity.ok(response);
	}
}