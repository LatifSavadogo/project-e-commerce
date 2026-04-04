package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ArticleAdminPatchDTO;
import net.ecommerce.springboot.dto.ArticleDTO;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.ArticleService;
import net.ecommerce.springboot.service.AuthService;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminArticleController {

	private final ArticleService articleService;
	private final AuthService authService;

	public AdminArticleController(ArticleService articleService, AuthService authService) {
		this.articleService = articleService;
		this.authService = authService;
	}

	@GetMapping("/articles")
	public ResponseEntity<List<ArticleDTO>> listAll() {
		return ResponseEntity.ok(articleService.listAllForAdmin());
	}

	@PatchMapping("/articles/{id}")
	public ResponseEntity<Void> patch(@PathVariable Integer id, @Valid @RequestBody ArticleAdminPatchDTO body) {
		User actor = authService.getCurrentUser();
		articleService.applyAdminPatch(actor, id, body.getBlocked(), body.getWarningMessage(),
				body.getClearWarning());
		return ResponseEntity.noContent().build();
	}
}
