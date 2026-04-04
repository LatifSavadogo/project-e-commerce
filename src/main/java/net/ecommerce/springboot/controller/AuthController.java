package net.ecommerce.springboot.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ForgotPasswordRequestDTO;
import net.ecommerce.springboot.dto.LoginRequest;
import net.ecommerce.springboot.dto.ResetPasswordRequestDTO;
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
import net.ecommerce.springboot.service.LivraisonService;
import net.ecommerce.springboot.service.PasswordResetService;
import net.ecommerce.springboot.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/cnib/";
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "docx");

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
			.getContextHolderStrategy();
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PaysRepository paysRepository;
	private final TypeArticleRepository typeArticleRepository;
	private final UserService userService;
	private final AuthService authService;
	private final PasswordResetService passwordResetService;

	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
			RoleRepository roleRepository, PaysRepository paysRepository, TypeArticleRepository typeArticleRepository,
			UserService userService, AuthService authService, PasswordResetService passwordResetService) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.paysRepository = paysRepository;
		this.typeArticleRepository = typeArticleRepository;
		this.userService = userService;
		this.authService = authService;
		this.passwordResetService = passwordResetService;
		Path dir = Paths.get(UPLOAD_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException ignored) {
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					body.getEmail().trim(), body.getPassword());
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContext context = securityContextHolderStrategy.createEmptyContext();
			context.setAuthentication(authentication);
			securityContextHolderStrategy.setContext(context);
			// Crée la session HTTP avant d’y stocker le SecurityContext (évite /me « déconnecté » aléatoire)
			request.getSession(true);
			securityContextRepository.saveContext(context, request, response);
			User u = userRepository.findByEmail(body.getEmail().trim())
					.orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));
			return ResponseEntity.ok(Map.of("authenticated", true, "user", UserDTO.fromEntity(u)));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Email ou mot de passe incorrect"));
		}
	}

	@PostMapping(value = "/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> register(@RequestParam String nom, @RequestParam String prenom,
			@RequestParam String email, @RequestParam String password, @RequestParam Integer idrole,
			@RequestParam(required = false) Integer idpays,
			@RequestParam(required = false) String ville,
			@RequestParam(required = false) Integer idtypeVendeur,
			@RequestParam(required = false) String typeEnginLivreur,
			@RequestParam("cnib") MultipartFile cnibFile) {
		try {
			if (userRepository.existsByEmail(email.trim())) {
				return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé."));
			}
			Role role = roleRepository.findById(idrole)
					.orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé : " + idrole));
			String lib = role.getLibrole();
			if (!RoleNames.ACHETEUR.equalsIgnoreCase(lib) && !RoleNames.VENDEUR.equalsIgnoreCase(lib)
					&& !RoleNames.LIVREUR.equalsIgnoreCase(lib)) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "Inscription réservée aux rôles ACHETEUR, VENDEUR ou LIVREUR."));
			}
			if (RoleNames.VENDEUR.equalsIgnoreCase(lib) && idtypeVendeur == null) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "Le vendeur doit choisir une catégorie (idtypeVendeur)."));
			}
			if (RoleNames.LIVREUR.equalsIgnoreCase(lib)
					&& (typeEnginLivreur == null || typeEnginLivreur.isBlank())) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "Le livreur doit indiquer typeEnginLivreur (MOTO ou VEHICULE)."));
			}
			if (cnibFile.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La CNI / pièce est requise."));
			}
			if (cnibFile.getSize() > MAX_FILE_SIZE) {
				return ResponseEntity.badRequest().body(Map.of("error", "Fichier trop volumineux (max 5 Mo)."));
			}
			String originalFileName = cnibFile.getOriginalFilename();
			String ext = getFileExtension(originalFileName);
			if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
				return ResponseEntity.badRequest().body(Map.of("error", "Extension non autorisée."));
			}
			String fileName = generateUniqueFileName(originalFileName, ext);
			Path dest = Paths.get(UPLOAD_DIR, fileName);
			Files.createDirectories(dest.getParent());
			Files.copy(cnibFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

			User user = new User();
			user.setNom(nom);
			user.setPrenom(prenom);
			user.setEmail(email.trim());
			user.setPassword(password);
			user.setCnib(fileName);
			user.setRole(role);
			user.setUserupdate("register");
			user.setDateupdate(LocalDateTime.now());
			if (idpays != null) {
				Pays pays = paysRepository.findById(idpays)
						.orElseThrow(() -> new ResourceNotFoundException("Pays introuvable : " + idpays));
				user.setPays(pays);
			}
			if (ville != null && !ville.isBlank()) {
				user.setVille(ville.trim());
			}
			if (RoleNames.VENDEUR.equalsIgnoreCase(lib)) {
				TypeArticle cat = typeArticleRepository.findById(idtypeVendeur).orElseThrow(
						() -> new ResourceNotFoundException("Type d'article introuvable : " + idtypeVendeur));
				user.setCategorieVendeur(cat);
			}
			if (RoleNames.LIVREUR.equalsIgnoreCase(lib)) {
				try {
					user.setTypeEnginLivreur(LivraisonService.parseTypeEngin(typeEnginLivreur));
				} catch (IllegalArgumentException ex) {
					return ResponseEntity.badRequest()
							.body(Map.of("error", "typeEnginLivreur invalide (attendu : MOTO ou VEHICULE)."));
				}
			} else {
				user.setTypeEnginLivreur(null);
			}
			User saved = userService.saveWithEncodedPassword(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(saved));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Erreur lors de l'enregistrement du fichier."));
		}
	}

	@GetMapping("/me")
	public ResponseEntity<?> me() {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.ok(Map.of("authenticated", false));
		}
		return ResponseEntity.ok(Map.of("authenticated", true, "user", UserDTO.fromEntity(u)));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO body) {
		passwordResetService.requestPasswordReset(body.getEmail());
		return ResponseEntity.ok(Map.of("message",
				"Si cette adresse est inscrite, un e-mail de réinitialisation a été envoyé."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO body) {
		try {
			passwordResetService.resetPasswordWithToken(body.getToken(), body.getNewPassword());
			return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour. Vous pouvez vous connecter."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "Lien invalide ou expiré. Demandez un nouvel e-mail."));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
		Map<String, Object> body = new HashMap<>();
		body.put("success", true);
		body.put("message", "Déconnexion réussie");
		return ResponseEntity.ok(body);
	}

	private static String getFileExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	/**
	 * Nom de fichier sûr (évite crash substring si nom absent / sans extension).
	 */
	private static String generateUniqueFileName(String originalFileName, String validatedExt) {
		String ext = validatedExt != null ? validatedExt.toLowerCase() : "bin";
		String base = "cnib";
		if (originalFileName != null && originalFileName.contains(".")) {
			int dot = originalFileName.lastIndexOf('.');
			if (dot > 0) {
				String raw = originalFileName.substring(0, dot).replaceAll("[^a-zA-Z0-9_-]", "_");
				if (!raw.isBlank()) {
					base = raw.length() > 80 ? raw.substring(0, 80) : raw;
				}
			}
		}
		return UUID.randomUUID() + "_" + base + "." + ext;
	}
}
