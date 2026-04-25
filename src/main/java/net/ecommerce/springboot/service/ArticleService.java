package net.ecommerce.springboot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.ecommerce.springboot.dto.ArticleDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.ArticleImage;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleImageRepository;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class ArticleService {

	public static final int MAX_ARTICLE_PHOTOS = 6;

	private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	private final ArticleRepository articleRepository;
	private final TypeArticleRepository typeArticleRepository;
	private final ArticleImageRepository articleImageRepository;
	private final SellerRatingService sellerRatingService;

	public ArticleService(ArticleRepository articleRepository, TypeArticleRepository typeArticleRepository,
			ArticleImageRepository articleImageRepository, SellerRatingService sellerRatingService) {
		this.articleRepository = articleRepository;
		this.typeArticleRepository = typeArticleRepository;
		this.articleImageRepository = articleImageRepository;
		this.sellerRatingService = sellerRatingService;
	}

	public List<ArticleDTO> listPublicCatalog(Boolean internationalOnly) {
		List<Article> source = Boolean.TRUE.equals(internationalOnly)
				? articleRepository.findByBlockedIsFalseAndVendeur_VendeurInternationalIsTrueOrderByDateupdateDesc()
				: articleRepository.findByBlockedIsFalseOrderByDateupdateDesc();
		List<ArticleDTO> dtos = new ArrayList<>(source.size());
		for (Article a : source) {
			dtos.add(toDtoWithImagesBasic(a));
		}
		enrichVendorBatch(source, dtos);
		return dtos;
	}

	public List<ArticleDTO> listAllForAdmin() {
		List<Article> source = articleRepository.findAllByOrderByDateupdateDesc();
		List<ArticleDTO> dtos = new ArrayList<>(source.size());
		for (Article a : source) {
			dtos.add(toDtoWithImagesBasic(a));
		}
		enrichVendorBatch(source, dtos);
		return dtos;
	}

	public ArticleDTO toDtoWithImages(Article article) {
		ArticleDTO dto = toDtoWithImagesBasic(article);
		enrichVendorBatch(List.of(article), List.of(dto));
		return dto;
	}

	private ArticleDTO toDtoWithImagesBasic(Article article) {
		if (article.getIdarticle() == null) {
			return ArticleDTO.fromEntity(article, List.of());
		}
		List<ArticleImage> imgs = articleImageRepository.findByArticle_IdarticleOrderBySortOrderAsc(article.getIdarticle());
		return ArticleDTO.fromEntity(article, imgs);
	}

	private void enrichVendorBatch(List<Article> articles, List<ArticleDTO> dtos) {
		if (articles.isEmpty()) {
			return;
		}
		Set<Integer> ids = sellerRatingService.collectSellerIds(articles);
		Map<Integer, double[]> agg = sellerRatingService.averageStarsBySellerIds(ids);
		LocalDateTime now = LocalDateTime.now();
		for (int i = 0; i < articles.size(); i++) {
			Article a = articles.get(i);
			ArticleDTO d = dtos.get(i);
			User v = a.getVendeur();
			if (v == null) {
				continue;
			}
			double[] z = agg.get(v.getIduser());
			if (z != null && z[1] > 0) {
				d.setVendeurNoteMoyenne(z[0]);
				d.setVendeurNombreAvis((int) Math.round(z[1]));
			} else {
				d.setVendeurNoteMoyenne(null);
				d.setVendeurNombreAvis(0);
			}
			d.setVendeurCertifieActif(
					v.getVendeurCertifieJusqua() != null && v.getVendeurCertifieJusqua().isAfter(now));
		}
	}

	@Transactional
	public ArticleDTO getForConsultation(Integer id, User currentUser) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
		if (article.isBlocked() && !isStaff(currentUser)) {
			throw new ResourceNotFoundException("Article non trouvé avec l'id : " + id);
		}
		article.setViewCount(article.getViewCount() + 1);
		articleRepository.save(article);
		return toDtoWithImages(article);
	}

	/** Vendeur : contrôle catégorie. Admin / super-admin : pas de contrainte. */
	public void assertCanCreateArticle(User actor, Integer idtype) {
		if (isStaff(actor)) {
			return;
		}
		assertSellerCanPublishInType(actor, idtype);
	}

	public void assertSellerCanPublishInType(User seller, Integer idtype) {
		if (seller.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(seller.getRole().getLibrole())) {
			throw new IllegalStateException("Seul un vendeur peut publier un article.");
		}
		if (idtype == null) {
			throw new IllegalArgumentException("La catégorie (type) est obligatoire.");
		}
		if (seller.getCategorieVendeur() == null) {
			throw new IllegalStateException("Le vendeur doit avoir une catégorie définie à l'inscription.");
		}
		if (seller.getCategorieVendeur().getIdtype() != idtype) {
			throw new IllegalStateException("Publication interdite hors de la catégorie du vendeur.");
		}
	}

	public void assertCanModifyArticle(User actor, Article article) {
		if (isStaff(actor)) {
			return;
		}
		if (actor == null || article.getVendeur() == null
				|| !article.getVendeur().getIduser().equals(actor.getIduser())) {
			throw new IllegalStateException("Action non autorisée sur cet article.");
		}
	}

	public void assertCanReadPhotoFile(Article article, String fileName, User currentUser) {
		assertCanReadPhoto(article, currentUser);
		if (fileName == null || fileName.isBlank() || fileName.contains("..")) {
			throw new ResourceNotFoundException("Fichier non trouvé.");
		}
		String normalized = Paths.get(fileName).getFileName().toString();
		if (!normalized.equals(fileName)) {
			throw new ResourceNotFoundException("Fichier non trouvé.");
		}
		boolean ok = normalized.equals(article.getPhoto());
		if (!ok && article.getIdarticle() != null) {
			ok = articleImageRepository.findByArticle_IdarticleOrderBySortOrderAsc(article.getIdarticle()).stream()
					.anyMatch(img -> normalized.equals(img.getUrl()));
		}
		if (!ok) {
			throw new ResourceNotFoundException("Fichier non trouvé.");
		}
	}

	public void assertCanReadPhoto(Article article, User currentUser) {
		if (article.isBlocked() && !isStaff(currentUser)) {
			throw new ResourceNotFoundException("Article non trouvé avec l'id : " + article.getIdarticle());
		}
	}

	public void assertTypeAllowedForSeller(User actor, Integer idtype) {
		if (isStaff(actor) || idtype == null) {
			return;
		}
		assertSellerCanPublishInType(actor, idtype);
	}

	public TypeArticle resolveType(Integer idtype) {
		if (idtype == null) {
			return null;
		}
		return typeArticleRepository.findById(idtype)
				.orElseThrow(() -> new ResourceNotFoundException("Type d'article non trouvé avec l'id : " + idtype));
	}

	@Transactional
	public void applyAdminPatch(User actor, Integer id, Boolean blocked, String warningMessage, Boolean clearWarning) {
		Article article = articleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
		if (Boolean.TRUE.equals(blocked) && actor != null && actor.getRole() != null
				&& RoleNames.ADMIN.equalsIgnoreCase(actor.getRole().getLibrole())
				&& !RoleNames.SUPER_ADMIN.equalsIgnoreCase(actor.getRole().getLibrole())) {
			User seller = article.getVendeur();
			if (seller != null && seller.getRole() != null) {
				String sr = seller.getRole().getLibrole();
				if (RoleNames.ADMIN.equalsIgnoreCase(sr) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(sr)) {
					throw new IllegalStateException(
							"Un administrateur ne peut pas bloquer une annonce publiée par un autre administrateur.");
				}
			}
		}
		if (blocked != null) {
			article.setBlocked(blocked);
		}
		if (Boolean.TRUE.equals(clearWarning)) {
			article.setWarningMessage(null);
		} else if (warningMessage != null) {
			article.setWarningMessage(warningMessage.isBlank() ? null : warningMessage.trim());
		}
		articleRepository.save(article);
	}

	public boolean isStaff(User user) {
		return isStaffImpl(user);
	}

	public static boolean isStaffImpl(User user) {
		if (user == null || user.getRole() == null) {
			return false;
		}
		String r = user.getRole().getLibrole();
		return RoleNames.ADMIN.equalsIgnoreCase(r) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(r);
	}

	public void validateGalleryFiles(List<MultipartFile> files) {
		if (files.isEmpty()) {
			throw new IllegalArgumentException("Au moins une photo est requise.");
		}
		if (files.size() > MAX_ARTICLE_PHOTOS) {
			throw new IllegalArgumentException("Maximum " + MAX_ARTICLE_PHOTOS + " photos par annonce.");
		}
		for (MultipartFile f : files) {
			validateOneImage(f);
		}
	}

	public void validateOneImage(MultipartFile f) {
		if (f.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("La taille d'un fichier dépasse 5 Mo.");
		}
		String ext = getFileExtension(f.getOriginalFilename());
		if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
			throw new IllegalArgumentException("Format non autorisé. Utilisez : " + ALLOWED_IMAGE_EXTENSIONS);
		}
	}

	public List<String> storeMultipartFiles(List<MultipartFile> files, String uploadDir) throws IOException {
		List<String> names = new ArrayList<>();
		Path dir = Paths.get(uploadDir);
		Files.createDirectories(dir);
		for (MultipartFile f : files) {
			String fileName = generateUniqueFileName(f.getOriginalFilename());
			Path path = dir.resolve(fileName).normalize();
			if (!path.startsWith(dir.normalize())) {
				throw new IOException("Chemin invalide.");
			}
			Files.copy(f.getInputStream(), path);
			names.add(fileName);
		}
		return names;
	}

	@Transactional
	public void createImageRows(Article article, List<String> storedFileNames) {
		for (int i = 0; i < storedFileNames.size(); i++) {
			ArticleImage img = new ArticleImage();
			img.setArticle(article);
			img.setUrl(storedFileNames.get(i));
			img.setIsPrimary(i == 0);
			img.setSortOrder(i);
			img.setUploadedAt(LocalDateTime.now());
			articleImageRepository.save(img);
		}
	}

	@Transactional
	public void replaceGallery(Article article, List<MultipartFile> files, String uploadDir) throws IOException {
		validateGalleryFiles(files);
		deleteArticleGallery(article, uploadDir);
		List<String> names = storeMultipartFiles(files, uploadDir);
		article.setPhoto(names.get(0));
		articleRepository.save(article);
		createImageRows(article, names);
	}

	@Transactional
	public void deleteArticleGallery(Article article, String uploadDir) {
		Integer id = article.getIdarticle();
		Set<String> toDelete = new LinkedHashSet<>();
		if (id != null) {
			for (ArticleImage img : articleImageRepository.findByArticle_IdarticleOrderBySortOrderAsc(id)) {
				toDelete.add(img.getUrl());
			}
			articleImageRepository.deleteByArticle_Idarticle(id);
		}
		if (article.getPhoto() != null && !article.getPhoto().isBlank()) {
			toDelete.add(article.getPhoto());
		}
		Path base = Paths.get(uploadDir).normalize();
		for (String f : toDelete) {
			try {
				Path p = base.resolve(f).normalize();
				if (p.startsWith(base)) {
					Files.deleteIfExists(p);
				}
			} catch (IOException ignored) {
			}
		}
	}

	private static String getFileExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	private static String generateUniqueFileName(String originalFileName) {
		if (originalFileName == null || !originalFileName.contains(".")) {
			return UUID.randomUUID() + "_upload.jpg";
		}
		String fileExtension = getFileExtension(originalFileName);
		String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
		return UUID.randomUUID() + "_" + baseName + "." + fileExtension;
	}
}
