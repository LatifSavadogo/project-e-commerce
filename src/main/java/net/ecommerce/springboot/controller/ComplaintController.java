package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ComplaintCreateDTO;
import net.ecommerce.springboot.dto.ComplaintDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.Complaint;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ComplaintRepository;
import net.ecommerce.springboot.service.AuthService;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

	private final ComplaintRepository complaintRepository;
	private final ArticleRepository articleRepository;
	private final AuthService authService;

	public ComplaintController(ComplaintRepository complaintRepository, ArticleRepository articleRepository,
			AuthService authService) {
		this.complaintRepository = complaintRepository;
		this.articleRepository = articleRepository;
		this.authService = authService;
	}

	@PostMapping
	public ResponseEntity<ComplaintDTO> create(@Valid @RequestBody ComplaintCreateDTO body) {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Complaint c = new Complaint();
		c.setAuteur(u);
		c.setTitre(body.getTitre().trim());
		c.setDescription(body.getDescription().trim());
		if (body.getIdArticle() != null) {
			Article a = articleRepository.findById(body.getIdArticle())
					.orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + body.getIdArticle()));
			c.setArticle(a);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(ComplaintDTO.fromEntity(complaintRepository.save(c)));
	}

	@GetMapping("/mine")
	public ResponseEntity<List<ComplaintDTO>> mine() {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(complaintRepository.findByAuteur_IduserOrderByDatecreationDesc(u.getIduser())
				.stream()
				.map(ComplaintDTO::fromEntity)
				.toList());
	}
}
