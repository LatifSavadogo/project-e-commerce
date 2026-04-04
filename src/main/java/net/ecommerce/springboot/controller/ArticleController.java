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

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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


import net.ecommerce.springboot.dto.ArticleDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1")
public class ArticleController {
    
    // Répertoire pour stocker les photos
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/articles/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private TypeArticleRepository typeArticleRepository;
    
    // Initialiser le répertoire d'upload
    public ArticleController() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    // Récupérer tous les articles avec DTO
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        List<ArticleDTO> articleDTOs = articles.stream()
            .map(ArticleDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(articleDTOs);
    }
    
    // Récupérer un article par ID avec DTO
    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Integer id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
        
        ArticleDTO articleDTO = ArticleDTO.fromEntity(article);
        return ResponseEntity.ok(articleDTO);
    }
    
    // Créer un article avec upload de photo
    @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createArticle(
            @RequestParam String libarticle,
            @RequestParam String descarticle,
            @RequestParam Integer prixunitaire,
            @RequestParam(required = false) Integer idtype,
            @RequestParam("photo") MultipartFile photoFile) {
        
        try {
            // Validation du fichier
            if (photoFile.isEmpty()) {
                return ResponseEntity.badRequest().body("La photo est requise");
            }
            
            if (photoFile.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
            }
            
            String originalFileName = photoFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            
            if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                return ResponseEntity.badRequest().body("Format de fichier non autorisé. Formats acceptés : " + ALLOWED_EXTENSIONS);
            }
            
            // Générer un nom de fichier unique
            String fileName = generateUniqueFileName(originalFileName);
            String filePath = UPLOAD_DIR + fileName;
            
            // Sauvegarder le fichier
            Path path = Paths.get(filePath);
            Files.copy(photoFile.getInputStream(), path);
            
            // Créer l'article
            Article article = new Article();
            article.setLibarticle(libarticle);
            article.setDescarticle(descarticle);
            article.setPrixunitaire(prixunitaire);
            article.setPhoto(fileName);
            article.setUserupdate("admin");
            article.setDateupdate(LocalDateTime.now());
            
            // Associer le type d'article si fourni
            if (idtype != null) {
                TypeArticle typeArticle = typeArticleRepository.findById(idtype)
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'article non trouvé avec l'id : " + idtype));
                article.setTypeArticle(typeArticle);
            }
            
            // Sauvegarder l'article
            Article savedArticle = articleRepository.save(article);
            ArticleDTO articleDTO = ArticleDTO.fromEntity(savedArticle);
            
            return ResponseEntity.ok(articleDTO);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }
    
    // Mettre à jour un article (avec possibilité de changer la photo)
    @PutMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateArticle(
            @PathVariable Integer id,
            @RequestParam String libarticle,
            @RequestParam String descarticle,
            @RequestParam Integer prixunitaire,
            @RequestParam(required = false) Integer idtype,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile) {
        
        try {
            // Vérifier si l'article existe
            Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
            
            // Mettre à jour les champs
            article.setLibarticle(libarticle);
            article.setDescarticle(descarticle);
            article.setPrixunitaire(prixunitaire);
            
            // Gérer la nouvelle photo si fournie
            if (photoFile != null && !photoFile.isEmpty()) {
                // Validation du fichier
                if (photoFile.getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
                }
                
                String originalFileName = photoFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                
                if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                    return ResponseEntity.badRequest().body("Format de fichier non autorisé. Formats acceptés : " + ALLOWED_EXTENSIONS);
                }
                
                // Supprimer l'ancienne photo si elle existe
                if (article.getPhoto() != null) {
                    String oldFilePath = UPLOAD_DIR + article.getPhoto();
                    File oldFile = new File(oldFilePath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                
                // Générer un nouveau nom de fichier et sauvegarder
                String fileName = generateUniqueFileName(originalFileName);
                String filePath = UPLOAD_DIR + fileName;
                Path path = Paths.get(filePath);
                Files.copy(photoFile.getInputStream(), path);
                
                article.setPhoto(fileName);
            }
            
            // Mettre à jour le type d'article si fourni
            if (idtype != null) {
                TypeArticle typeArticle = typeArticleRepository.findById(idtype)
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'article non trouvé avec l'id : " + idtype));
                article.setTypeArticle(typeArticle);
            } else {
                article.setTypeArticle(null);
            }
            
            // Sauvegarder les modifications
            Article updatedArticle = articleRepository.save(article);
            ArticleDTO articleDTO = ArticleDTO.fromEntity(updatedArticle);
            
            return ResponseEntity.ok(articleDTO);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }
    
    // Mettre à jour un article via JSON (sans changer la photo)
    @PutMapping("/articles/{id}/json")
    public ResponseEntity<ArticleDTO> updateArticleJson(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleDTO articleDTO) {
        
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
        
        article.setLibarticle(articleDTO.getLibarticle());
        article.setDescarticle(articleDTO.getDescarticle());
        article.setPrixunitaire(articleDTO.getPrixunitaire());
        article.setUserupdate(articleDTO.getUserupdate());
        
        if (articleDTO.getIdtype() != null) {
            TypeArticle typeArticle = typeArticleRepository.findById(articleDTO.getIdtype())
                .orElseThrow(() -> new ResourceNotFoundException("Type d'article non trouvé avec l'id : " + articleDTO.getIdtype()));
            article.setTypeArticle(typeArticle);
        }
        
        Article updatedArticle = articleRepository.save(article);
        ArticleDTO updatedDTO = ArticleDTO.fromEntity(updatedArticle);
        
        return ResponseEntity.ok(updatedDTO);
    }
    
    // Supprimer un article
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteArticle(@PathVariable Integer id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
        
        // Supprimer la photo associée
        if (article.getPhoto() != null) {
            String filePath = UPLOAD_DIR + article.getPhoto();
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        
        articleRepository.delete(article);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("supprimé", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
    
    // Télécharger la photo d'un article
    @GetMapping("/articles/{id}/photo")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Integer id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id : " + id));
        
        if (article.getPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(article.getPhoto()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + article.getPhoto() + "\"")
                .body(resource);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Méthodes utilitaires
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        return UUID.randomUUID().toString() + "_" + baseName + "." + fileExtension;
    }
}