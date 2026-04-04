package net.ecommerce.springboot.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.AdminPasswordResetDTO;
import net.ecommerce.springboot.dto.AdminUserPatchDTO;
import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Pays;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.PaysRepository;
import net.ecommerce.springboot.repository.RoleRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.GdprExportService;
import net.ecommerce.springboot.service.LivraisonService;
import net.ecommerce.springboot.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {

	private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/cnib/";
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "docx");

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PaysRepository paysRepository;
	private final TypeArticleRepository typeArticleRepository;
	private final UserService userService;
	private final AuthService authService;
	private final GdprExportService gdprExportService;

	public UserController(UserRepository userRepository, RoleRepository roleRepository, PaysRepository paysRepository,
			TypeArticleRepository typeArticleRepository, UserService userService, AuthService authService,
			GdprExportService gdprExportService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.paysRepository = paysRepository;
		this.typeArticleRepository = typeArticleRepository;
		this.userService = userService;
		this.authService = authService;
		this.gdprExportService = gdprExportService;
		File uploadDir = new File(UPLOAD_DIR);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
	}

	@GetMapping("/users/me/data-export")
	public ResponseEntity<Map<String, Object>> exportMyData() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		Map<String, Object> payload = gdprExportService.buildExport(u);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"ecomarket-donnees-utilisateur-" + u.getIduser() + ".json\"")
				.body(payload);
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@GetMapping("/users")
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		return ResponseEntity.ok(userRepository.findAll().stream().map(UserDTO::fromEntity).toList());
	}

	@PreAuthorize("@authService.isSelf(#id) or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@GetMapping("/users/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + id));
		return ResponseEntity.ok(UserDTO.fromEntity(user));
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createUser(@RequestParam String nom, @RequestParam String prenom,
			@RequestParam String email, @RequestParam String password,
			@RequestParam(required = false) Integer idrole, @RequestParam(required = false) Integer idpays,
			@RequestParam(required = false) Integer idtypeVendeur,
			@RequestParam(required = false) String typeEnginLivreur,
			@RequestParam("cnib") MultipartFile cnibFile) {
		try {
			if (cnibFile.isEmpty()) {
				return ResponseEntity.badRequest().body("La cnib est requise");
			}
			if (cnibFile.getSize() > MAX_FILE_SIZE) {
				return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
			}
			String originalFileName = cnibFile.getOriginalFilename();
			String fileExtension = getFileExtension(originalFileName);
			if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
				return ResponseEntity.badRequest().body("Format de fichier non autorisé.");
			}
			String fileName = generateUniqueFileName(originalFileName);
			Files.copy(cnibFile.getInputStream(), Paths.get(UPLOAD_DIR + fileName));

			User user = new User();
			user.setNom(nom);
			user.setPrenom(prenom);
			user.setPassword(password);
			user.setEmail(email);
			user.setCnib(fileName);
			user.setUserupdate(authService.getCurrentUserEmail());
			user.setDateupdate(LocalDateTime.now());
			if (idrole != null) {
				Role role = roleRepository.findById(idrole)
						.orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + idrole));
				user.setRole(role);
				if (RoleNames.VENDEUR.equalsIgnoreCase(role.getLibrole())) {
					if (idtypeVendeur == null) {
						return ResponseEntity.badRequest().body("idtypeVendeur requis pour un vendeur.");
					}
					TypeArticle cat = typeArticleRepository.findById(idtypeVendeur).orElseThrow(
							() -> new ResourceNotFoundException("Type d'article introuvable : " + idtypeVendeur));
					user.setCategorieVendeur(cat);
				}
				if (RoleNames.LIVREUR.equalsIgnoreCase(role.getLibrole())) {
					if (typeEnginLivreur == null || typeEnginLivreur.isBlank()) {
						return ResponseEntity.badRequest().body("typeEnginLivreur requis (MOTO ou VEHICULE) pour un livreur.");
					}
					try {
						user.setTypeEnginLivreur(LivraisonService.parseTypeEngin(typeEnginLivreur));
					} catch (IllegalArgumentException ex) {
						return ResponseEntity.badRequest().body("typeEnginLivreur invalide.");
					}
				} else {
					user.setTypeEnginLivreur(null);
				}
			}
			if (idpays != null) {
				Pays pays = paysRepository.findById(idpays)
						.orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + idpays));
				user.setPays(pays);
			}
			User saved = userService.saveWithEncodedPassword(user);
			return ResponseEntity.ok(UserDTO.fromEntity(saved));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors du téléchargement du fichier : " + e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping(value = "/users/{iduser}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateUser(@PathVariable Integer iduser, @RequestParam String nom,
			@RequestParam String prenom, @RequestParam String email, @RequestParam String password,
			@RequestParam(required = false) Integer idrole, @RequestParam(required = false) Integer idpays,
			@RequestParam(required = false) Integer idtypeVendeur,
			@RequestParam(required = false) String typeEnginLivreur,
			@RequestParam(value = "cnib", required = false) MultipartFile cnibFile) {
		try {
			User user = userRepository.findById(iduser)
					.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
			user.setNom(nom);
			user.setPrenom(prenom);
			user.setPassword(password);
			user.setEmail(email);
			if (cnibFile != null && !cnibFile.isEmpty()) {
				if (cnibFile.getSize() > MAX_FILE_SIZE) {
					return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
				}
				String originalFileName = cnibFile.getOriginalFilename();
				String fileExtension = getFileExtension(originalFileName);
				if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
					return ResponseEntity.badRequest().body("Format de fichier non autorisé.");
				}
				if (user.getCnib() != null) {
					File oldFile = new File(UPLOAD_DIR + user.getCnib());
					if (oldFile.exists()) {
						oldFile.delete();
					}
				}
				String fileName = generateUniqueFileName(originalFileName);
				Files.copy(cnibFile.getInputStream(), Paths.get(UPLOAD_DIR + fileName));
				user.setCnib(fileName);
			}
			if (idrole != null) {
				Role role = roleRepository.findById(idrole)
						.orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + idrole));
				user.setRole(role);
				if (RoleNames.VENDEUR.equalsIgnoreCase(role.getLibrole())) {
					if (idtypeVendeur != null) {
						TypeArticle cat = typeArticleRepository.findById(idtypeVendeur).orElseThrow(
								() -> new ResourceNotFoundException("Type d'article introuvable : " + idtypeVendeur));
						user.setCategorieVendeur(cat);
					}
				} else {
					user.setCategorieVendeur(null);
				}
				if (RoleNames.LIVREUR.equalsIgnoreCase(role.getLibrole())) {
					if (typeEnginLivreur != null && !typeEnginLivreur.isBlank()) {
						try {
							user.setTypeEnginLivreur(LivraisonService.parseTypeEngin(typeEnginLivreur));
						} catch (IllegalArgumentException ex) {
							return ResponseEntity.badRequest().body("typeEnginLivreur invalide.");
						}
					}
				} else {
					user.setTypeEnginLivreur(null);
				}
			} else if (idtypeVendeur != null && user.getRole() != null
					&& RoleNames.VENDEUR.equalsIgnoreCase(user.getRole().getLibrole())) {
				TypeArticle cat = typeArticleRepository.findById(idtypeVendeur).orElseThrow(
						() -> new ResourceNotFoundException("Type d'article introuvable : " + idtypeVendeur));
				user.setCategorieVendeur(cat);
			} else if (typeEnginLivreur != null && !typeEnginLivreur.isBlank() && user.getRole() != null
					&& RoleNames.LIVREUR.equalsIgnoreCase(user.getRole().getLibrole())) {
				try {
					user.setTypeEnginLivreur(LivraisonService.parseTypeEngin(typeEnginLivreur));
				} catch (IllegalArgumentException ex) {
					return ResponseEntity.badRequest().body("typeEnginLivreur invalide.");
				}
			}
			if (idpays != null) {
				user.setPays(paysRepository.findById(idpays)
						.orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + idpays)));
			} else {
				user.setPays(null);
			}
			user.setUserupdate(authService.getCurrentUserEmail());
			user.setDateupdate(LocalDateTime.now());
			return ResponseEntity.ok(UserDTO.fromEntity(userService.saveWithEncodedPassword(user)));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erreur lors du téléchargement du fichier : " + e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping("/users/{iduser}/json")
	public ResponseEntity<UserDTO> updateUserJson(@PathVariable Integer iduser, @Valid @RequestBody UserDTO userDTO) {
		User user = userRepository.findById(iduser)
				.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
		user.setNom(userDTO.getNom());
		user.setPrenom(userDTO.getPrenom());
		user.setPassword(userDTO.getPassword());
		user.setEmail(userDTO.getEmail());
		user.setUserupdate(userDTO.getUserupdate() != null ? userDTO.getUserupdate() : authService.getCurrentUserEmail());
		if (userDTO.getIdrole() != null) {
			Role role = roleRepository.findById(userDTO.getIdrole())
					.orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + userDTO.getIdrole()));
			user.setRole(role);
			String rl = role.getLibrole();
			if (!RoleNames.VENDEUR.equalsIgnoreCase(rl)) {
				user.setCategorieVendeur(null);
			}
			if (!RoleNames.LIVREUR.equalsIgnoreCase(rl)) {
				user.setTypeEnginLivreur(null);
			}
		}
		if (userDTO.getIdpays() != null) {
			user.setPays(paysRepository.findById(userDTO.getIdpays()).orElseThrow(
					() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + userDTO.getIdpays())));
		}
		if (userDTO.getIdtypeVendeur() != null) {
			TypeArticle cat = typeArticleRepository.findById(userDTO.getIdtypeVendeur()).orElseThrow(
					() -> new ResourceNotFoundException("Type d'article introuvable : " + userDTO.getIdtypeVendeur()));
			user.setCategorieVendeur(cat);
		}
		if (userDTO.getVille() != null) {
			String v = userDTO.getVille().trim();
			user.setVille(v.isEmpty() ? null : v);
		}
		if (userDTO.getTypeEnginLivreur() != null && !userDTO.getTypeEnginLivreur().isBlank()) {
			if (user.getRole() == null || !RoleNames.LIVREUR.equalsIgnoreCase(user.getRole().getLibrole())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Engin réservé aux livreurs.");
			}
			try {
				user.setTypeEnginLivreur(LivraisonService.parseTypeEngin(userDTO.getTypeEnginLivreur()));
			} catch (IllegalArgumentException ex) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "typeEnginLivreur invalide.");
			}
		}
		user.setDateupdate(LocalDateTime.now());
		return ResponseEntity.ok(UserDTO.fromEntity(userService.saveWithEncodedPassword(user)));
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PatchMapping("/users/{iduser}/admin-profile")
	public ResponseEntity<UserDTO> adminPatchProfile(@PathVariable Integer iduser,
			@RequestBody AdminUserPatchDTO body) {
		User admin = authService.getCurrentUser();
		if (admin == null) {
			throw new IllegalStateException("Non authentifié");
		}
		try {
			User updated = userService.adminPatchUser(admin, iduser, body);
			return ResponseEntity.ok(UserDTO.fromEntity(updated));
		} catch (IllegalStateException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@PutMapping("/users/{id}/admin-password")
	public ResponseEntity<UserDTO> adminResetPassword(@PathVariable Integer id,
			@Valid @RequestBody AdminPasswordResetDTO body) {
		userService.resetPassword(id, body.getNewPassword());
		User u = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + id));
		return ResponseEntity.ok(UserDTO.fromEntity(u));
	}

	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	@DeleteMapping("/users/{iduser}")
	public ResponseEntity<?> deleteUser(@PathVariable Integer iduser) {
		User admin = authService.getCurrentUser();
		try {
			User target = userRepository.findById(iduser)
					.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
			if (target.getCnib() != null) {
				File file = new File(UPLOAD_DIR + target.getCnib());
				if (file.exists()) {
					file.delete();
				}
			}
			userService.deleteUserAsAdmin(admin, iduser);
			Map<String, Boolean> response = new HashMap<>();
			response.put("supprimé", Boolean.TRUE);
			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@PreAuthorize("@authService.isSelf(#iduser) or hasAnyRole('ADMIN','SUPER_ADMIN')")
	@GetMapping("/users/{iduser}/cnib")
	public ResponseEntity<Resource> downloadCnib(@PathVariable Integer iduser) {
		User user = userRepository.findById(iduser)
				.orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
		if (user.getCnib() == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			Path filePath = Paths.get(UPLOAD_DIR).resolve(user.getCnib()).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}
			String contentType = Files.probeContentType(filePath);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + user.getCnib() + "\"")
					.body(resource);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private static String getFileExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	private static String generateUniqueFileName(String originalFileName) {
		String fileExtension = getFileExtension(originalFileName);
		String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
		return UUID.randomUUID() + "_" + baseName + "." + fileExtension;
	}
}
