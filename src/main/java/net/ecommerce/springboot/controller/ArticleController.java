package net.ecommerce.springboot.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ArticleDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.service.ArticleService;
import net.ecommerce.springboot.service.AuthService;

@RestController
@RequestMapping("/api/v1")
public class ArticleController {

	private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/articles/";

	private final ArticleRepository articleRepository;
	private final ArticleService articleService;
	private final AuthService authService;

	public ArticleController(ArticleRepository articleRepository, ArticleService articleService,
			AuthService authService) {
		this.articleRepository = articleRepository;
		this.articleService = articleService;
		this.authService = authService;
		try {
			Files.createDirectories(Paths.get(UPLOAD_DIR));
		} catch (IOException ignored) {
		}
	}

	@GetMapping("/articles")
	public ResponseEntity<List<ArticleDTO>> getAllArticles(
			@RequestParam(name = "international", required = false) Boolean international) {
		return ResponseEntity.ok(articleService.listPublicCatalog(international));
	}

	@GetMapping("/articles/{id}")
	public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Integer id) {
		User current = authService.getCurrentUser();
		return ResponseEntity.ok(articleService.getForConsultation(id, current));
	}

	@PreAuthorize("hasRole('VENDEUR') or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createArticle(@RequestParam String libarticle, @RequestParam String descarticle,
			@RequestParam Integer prixunitaire, @RequestParam(required = false) Integer idtype,
			@RequestParam(value = "photo", required = false) MultipartFile photoFile,
			@RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
		User actor = authService.getCurrentUser();
		try {
			articleService.assertCanCreateArticle(actor, idtype);
			List<MultipartFile> gallery = resolveGalleryInput(photoFile, photos);
			articleService.validateGalleryFiles(gallery);
			List<String> names = articleService.storeMultipartFiles(gallery, UPLOAD_DIR);

			Article article = new Article();
			article.setLibarticle(libarticle);
			article.setDescarticle(descarticle);
			article.setPrixunitaire(prixunitaire);
			article.setPhoto(names.get(0));
			article.setUserupdate(actor != null ? actor.getEmail() : "system");
			article.setDateupdate(LocalDateTime.now());
			if (idtype != null) {
				TypeArticle typeArticle = articleService.resolveType(idtype);
				article.setTypeArticle(typeArticle);
			}
			if (articleService.isStaff(actor)) {
				article.setVendeur(null);
			} else {
				article.setVendeur(actor);
			}
			Article savedArticle = articleRepository.save(article);
			articleService.createImageRows(savedArticle, names);
			return ResponseEntity.ok(articleService.toDtoWithImages(savedArticle));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors du téléchargement du fichier : " + e.getMessage());
		}
	}

	@PreAuthorize("hasRole('VENDEUR') or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateArticle(@PathVariable Integer id, @RequestParam String libarticle,
			@RequestParam String descarticle, @RequestParam Integer prixunitaire,
			@RequestParam(required = false) Integer idtype,
			@RequestParam(value = "photo", required = false) MultipartFile photoFile,
			@RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
		User actor = authService.getCurrentUser();
		try {
			Article article = articleRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
			articleService.assertCanModifyArticle(actor, article);
			articleService.assertTypeAllowedForSeller(actor, idtype);
			article.setLibarticle(libarticle);
			article.setDescarticle(descarticle);
			article.setPrixunitaire(prixunitaire);
			if (idtype != null) {
				article.setTypeArticle(articleService.resolveType(idtype));
			} else {
				article.setTypeArticle(null);
			}
			article.setUserupdate(actor != null ? actor.getEmail() : "system");
			article.setDateupdate(LocalDateTime.now());

			List<MultipartFile> gallery = resolveGalleryInput(photoFile, photos);
			if (!gallery.isEmpty()) {
				articleService.replaceGallery(article, gallery, UPLOAD_DIR);
			}
			return ResponseEntity.ok(articleService.toDtoWithImages(articleRepository.save(article)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors du téléchargement du fichier : " + e.getMessage());
		}
	}

	@PreAuthorize("hasRole('VENDEUR') or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping("/articles/{id}/json")
	public ResponseEntity<?> updateArticleJson(@PathVariable Integer id, @Valid @RequestBody ArticleDTO articleDTO) {
		User actor = authService.getCurrentUser();
		try {
			Article article = articleRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
			articleService.assertCanModifyArticle(actor, article);
			articleService.assertTypeAllowedForSeller(actor, articleDTO.getIdtype());
			article.setLibarticle(articleDTO.getLibarticle());
			article.setDescarticle(articleDTO.getDescarticle());
			article.setPrixunitaire(articleDTO.getPrixunitaire());
			if (articleDTO.getUserupdate() != null) {
				article.setUserupdate(articleDTO.getUserupdate());
			}
			if (articleDTO.getIdtype() != null) {
				article.setTypeArticle(articleService.resolveType(articleDTO.getIdtype()));
			}
			article.setDateupdate(LocalDateTime.now());
			return ResponseEntity.ok(articleService.toDtoWithImages(articleRepository.save(article)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@PreAuthorize("hasRole('VENDEUR') or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@DeleteMapping("/articles/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteArticle(@PathVariable Integer id) {
		User actor = authService.getCurrentUser();
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
		articleService.assertCanModifyArticle(actor, article);
		articleService.deleteArticleGallery(article, UPLOAD_DIR);
		articleRepository.delete(article);
		Map<String, Boolean> response = new HashMap<>();
		response.put("supprimé", Boolean.TRUE);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/articles/{id}/photo")
	public ResponseEntity<Resource> downloadPhoto(@PathVariable Integer id) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
		articleService.assertCanReadPhoto(article, authService.getCurrentUser());
		if (article.getPhoto() == null) {
			return ResponseEntity.notFound().build();
		}
		return serveFile(article.getPhoto());
	}

	@GetMapping("/articles/{id}/photo/{filename:.+}")
	public ResponseEntity<Resource> downloadPhotoByName(@PathVariable Integer id, @PathVariable String filename) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
		articleService.assertCanReadPhotoFile(article, filename, authService.getCurrentUser());
		return serveFile(filename);
	}

	private ResponseEntity<Resource> serveFile(String storedName) {
		try {
			Path base = Paths.get(UPLOAD_DIR).normalize();
			Path filePath = base.resolve(storedName).normalize();
			if (!filePath.startsWith(base)) {
				return ResponseEntity.notFound().build();
			}
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}
			String contentType = Files.probeContentType(filePath);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedName + "\"")
					.body(resource);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private static List<MultipartFile> resolveGalleryInput(MultipartFile single, List<MultipartFile> many) {
		if (many != null && !many.isEmpty()) {
			List<MultipartFile> nonEmpty = new ArrayList<>();
			for (MultipartFile f : many) {
				if (f != null && !f.isEmpty()) {
					nonEmpty.add(f);
				}
			}
			if (!nonEmpty.isEmpty()) {
				return nonEmpty;
			}
		}
		if (single != null && !single.isEmpty()) {
			return List.of(single);
		}
		return List.of();
	}
}
