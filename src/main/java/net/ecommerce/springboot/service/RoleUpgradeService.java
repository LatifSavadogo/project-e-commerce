package net.ecommerce.springboot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.model.RoleUpgradeRequest;
import net.ecommerce.springboot.model.RoleUpgradeStatus;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.RoleRepository;
import net.ecommerce.springboot.repository.RoleUpgradeRequestRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.util.GeoCoordinates;

@Service
public class RoleUpgradeService {

	public static final String PENDING_DIR = System.getProperty("user.dir") + "/uploads/role-upgrade-pending/";
	public static final String CNIB_DIR = System.getProperty("user.dir") + "/uploads/cnib/";
	public static final String PHOTO_DIR = System.getProperty("user.dir") + "/uploads/photos/";

	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
	private static final List<String> CNIB_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "docx");
	private static final List<String> PHOTO_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

	private final RoleUpgradeRequestRepository requestRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TypeArticleRepository typeArticleRepository;
	private final UserService userService;

	public RoleUpgradeService(RoleUpgradeRequestRepository requestRepository, UserRepository userRepository,
			RoleRepository roleRepository, TypeArticleRepository typeArticleRepository, UserService userService) {
		this.requestRepository = requestRepository;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.typeArticleRepository = typeArticleRepository;
		this.userService = userService;
		initDirs();
	}

	private void initDirs() {
		try {
			Files.createDirectories(Paths.get(PENDING_DIR));
			Files.createDirectories(Paths.get(CNIB_DIR));
			Files.createDirectories(Paths.get(PHOTO_DIR));
		} catch (IOException ignored) {
		}
	}

	@Transactional
	public RoleUpgradeRequest submit(User acheteur, String roleDemande, MultipartFile cnibFile, MultipartFile photoFile,
			Double latitude, Double longitude, Integer idtypeVendeur, String typeEnginLivreurRaw, boolean vendeurInternational)
			throws IOException {
		if (acheteur.getRole() == null || !RoleNames.ACHETEUR.equalsIgnoreCase(acheteur.getRole().getLibrole())) {
			throw new IllegalStateException("Seuls les comptes acheteur peuvent demander une mise à niveau.");
		}
		String rd = roleDemande != null ? roleDemande.trim().toUpperCase() : "";
		if (!RoleNames.VENDEUR.equals(rd) && !RoleNames.LIVREUR.equals(rd)) {
			throw new IllegalArgumentException("Rôle demandé invalide (VENDEUR ou LIVREUR).");
		}
		if (requestRepository.existsByUser_IduserAndStatus(acheteur.getIduser(), RoleUpgradeStatus.PENDING)) {
			throw new IllegalStateException("Une demande est déjà en attente de validation.");
		}
		validateFiles(cnibFile, photoFile);
		if (RoleNames.VENDEUR.equals(rd)) {
			if (idtypeVendeur == null) {
				throw new IllegalArgumentException("Catégorie vendeur (idtypeVendeur) requise.");
			}
			if (latitude == null || longitude == null) {
				throw new IllegalArgumentException("Latitude et longitude (emplacement GPS) requis pour le profil vendeur.");
			}
			GeoCoordinates.assertValidRange(latitude, longitude);
		}
		if (RoleNames.LIVREUR.equals(rd)) {
			if (typeEnginLivreurRaw == null || typeEnginLivreurRaw.isBlank()) {
				throw new IllegalArgumentException("Type d'engin requis (MOTO ou VEHICULE) pour le livreur.");
			}
			LivraisonService.parseTypeEngin(typeEnginLivreurRaw);
		}

		String cnibName = storePending(cnibFile, "cnib", CNIB_EXT);
		String photoName = storePending(photoFile, "photo", PHOTO_EXT);

		RoleUpgradeRequest req = new RoleUpgradeRequest();
		req.setUser(acheteur);
		req.setRoleDemande(rd);
		req.setStatus(RoleUpgradeStatus.PENDING);
		req.setCnibFilename(cnibName);
		req.setPhotoFilename(photoName);
		req.setLatitude(latitude);
		req.setLongitude(longitude);
		req.setIdtypeVendeur(RoleNames.VENDEUR.equals(rd) ? idtypeVendeur : null);
		req.setVendeurInternational(RoleNames.VENDEUR.equals(rd) && vendeurInternational);
		if (RoleNames.LIVREUR.equals(rd)) {
			req.setTypeEnginLivreur(LivraisonService.parseTypeEngin(typeEnginLivreurRaw));
		}
		return requestRepository.save(req);
	}

	private void validateFiles(MultipartFile cnibFile, MultipartFile photoFile) {
		if (cnibFile == null || cnibFile.isEmpty()) {
			throw new IllegalArgumentException("Le fichier CNIB / pièce d'identité est requis.");
		}
		if (photoFile == null || photoFile.isEmpty()) {
			throw new IllegalArgumentException("La photo est requise.");
		}
		checkFile(cnibFile, CNIB_EXT, "CNIB");
		checkFile(photoFile, PHOTO_EXT, "photo");
	}

	private void checkFile(MultipartFile f, List<String> allowedExt, String label) {
		if (f.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("Fichier " + label + " trop volumineux (max 5 Mo).");
		}
		String ext = extensionOf(f.getOriginalFilename());
		if (!allowedExt.contains(ext.toLowerCase())) {
			throw new IllegalArgumentException("Extension non autorisée pour " + label + ".");
		}
	}

	private String storePending(MultipartFile file, String prefix, List<String> allowedExt) throws IOException {
		String ext = extensionOf(file.getOriginalFilename()).toLowerCase();
		if (!allowedExt.contains(ext)) {
			throw new IllegalArgumentException("Extension fichier invalide.");
		}
		String name = UUID.randomUUID() + "_" + prefix + "." + ext;
		Path dest = Paths.get(PENDING_DIR, name);
		Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
		return name;
	}

	private static String extensionOf(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	@Transactional(readOnly = true)
	public java.util.Optional<RoleUpgradeRequest> findLatestForUser(Integer iduser) {
		return requestRepository.findTopByUser_IduserOrderByCreatedAtDesc(iduser);
	}

	@Transactional(readOnly = true)
	public List<RoleUpgradeRequest> listForAdmin(RoleUpgradeStatus status) {
		if (status == null) {
			return requestRepository.findAll().stream()
					.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
					.toList();
		}
		return requestRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Transactional
	public User approve(Integer requestId, User admin) {
		RoleUpgradeRequest req = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Demande introuvable."));
		if (req.getStatus() != RoleUpgradeStatus.PENDING) {
			throw new IllegalStateException("Cette demande n'est plus en attente.");
		}
		User user = userRepository.findById(req.getUser().getIduser())
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
		if (user.getRole() == null || !RoleNames.ACHETEUR.equalsIgnoreCase(user.getRole().getLibrole())) {
			throw new IllegalStateException("Le compte n'est plus acheteur ; impossible d'approuver.");
		}
		if (RoleNames.VENDEUR.equalsIgnoreCase(req.getRoleDemande())) {
			if (req.getLatitude() == null || req.getLongitude() == null) {
				throw new IllegalStateException(
						"Demande vendeur sans coordonnées GPS : refusez la demande ou demandez une nouvelle candidature.");
			}
			GeoCoordinates.assertValidRange(req.getLatitude(), req.getLongitude());
		}

		Path pendingCnib = Paths.get(PENDING_DIR, req.getCnibFilename()).normalize();
		Path pendingPhoto = Paths.get(PENDING_DIR, req.getPhotoFilename()).normalize();
		if (!Files.exists(pendingCnib) || !Files.exists(pendingPhoto)) {
			throw new IllegalStateException("Fichiers de la demande introuvables sur le serveur.");
		}

		String newCnib = moveToDir(pendingCnib, CNIB_DIR, "cnib");
		String newPhoto = moveToDir(pendingPhoto, PHOTO_DIR, "profil");

		deleteOldUserFiles(user);

		user.setCnib(newCnib);
		user.setPhotoProfil(newPhoto);
		if (req.getLatitude() != null) {
			user.setLatitude(req.getLatitude());
		}
		if (req.getLongitude() != null) {
			user.setLongitude(req.getLongitude());
		}

		Role newRole = roleRepository.findByLibroleIgnoreCase(req.getRoleDemande())
				.orElseThrow(() -> new IllegalArgumentException("Rôle cible introuvable."));
		user.setRole(newRole);
		if (RoleNames.VENDEUR.equalsIgnoreCase(req.getRoleDemande())) {
			Integer tid = req.getIdtypeVendeur();
			if (tid == null) {
				throw new IllegalStateException("Type vendeur manquant sur la demande.");
			}
			TypeArticle cat = typeArticleRepository.findById(tid)
					.orElseThrow(() -> new IllegalArgumentException("Type d'article introuvable."));
			user.setCategorieVendeur(cat);
			user.setTypeEnginLivreur(null);
			user.setVendeurInternational(req.isVendeurInternational());
		} else if (RoleNames.LIVREUR.equalsIgnoreCase(req.getRoleDemande())) {
			user.setCategorieVendeur(null);
			user.setTypeEnginLivreur(req.getTypeEnginLivreur());
			user.setVendeurInternational(false);
		}
		user.setUserupdate(admin != null ? admin.getEmail() : "admin");
		user.setDateupdate(LocalDateTime.now());

		req.setStatus(RoleUpgradeStatus.APPROVED);
		req.setDecidedAt(LocalDateTime.now());
		requestRepository.save(req);

		return userService.saveWithEncodedPassword(user);
	}

	private void deleteOldUserFiles(User user) {
		try {
			if (user.getCnib() != null) {
				Path p = Paths.get(CNIB_DIR, user.getCnib()).normalize();
				Files.deleteIfExists(p);
			}
			if (user.getPhotoProfil() != null) {
				Path p = Paths.get(PHOTO_DIR, user.getPhotoProfil()).normalize();
				Files.deleteIfExists(p);
			}
		} catch (IOException ignored) {
		}
	}

	private String moveToDir(Path source, String targetDir, String logicalPrefix) {
		try {
			String ext = extensionOf(source.getFileName().toString()).toLowerCase();
			String name = UUID.randomUUID() + "_" + logicalPrefix + "." + ext;
			Path dest = Paths.get(targetDir, name).normalize();
			Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
			return name;
		} catch (IOException e) {
			throw new IllegalStateException("Impossible de finaliser les fichiers : " + e.getMessage());
		}
	}

	@Transactional
	public void reject(Integer requestId, User admin, String motif) {
		RoleUpgradeRequest req = requestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Demande introuvable."));
		if (req.getStatus() != RoleUpgradeStatus.PENDING) {
			throw new IllegalStateException("Cette demande n'est plus en attente.");
		}
		try {
			Files.deleteIfExists(Paths.get(PENDING_DIR, req.getCnibFilename()).normalize());
			Files.deleteIfExists(Paths.get(PENDING_DIR, req.getPhotoFilename()).normalize());
		} catch (IOException ignored) {
		}
		req.setStatus(RoleUpgradeStatus.REJECTED);
		req.setAdminMotif(motif);
		req.setDecidedAt(LocalDateTime.now());
		requestRepository.save(req);
	}
}
