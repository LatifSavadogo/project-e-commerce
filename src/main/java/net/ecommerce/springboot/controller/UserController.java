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


import net.ecommerce.springboot.dto.UserDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.model.Pays;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.repository.PaysRepository;
import net.ecommerce.springboot.repository.RoleRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1")
public class UserController {
    
    // Répertoire pour stocker les photos des cnib
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/cnib/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif","pdf","docx");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PaysRepository paysRepository;
    
    // Initialiser le répertoire d'upload
    public UserController() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    // Récupérer tous les users avec DTO
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
            .map(UserDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(userDTOs);
    }
    
    // Récupérer un user par ID avec DTO
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + id));
        
        UserDTO userDTO = UserDTO.fromEntity(user);
        return ResponseEntity.ok(userDTO);
    }
    
    // Créer un user avec upload de photo
    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createUser(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) Integer idrole,
            @RequestParam(required = false) Integer idpays,
            @RequestParam("cnib") MultipartFile cnibFile) {
        
        try {
            // Validation du fichier
            if (cnibFile.isEmpty()) {
                return ResponseEntity.badRequest().body("La cnib est requise");
            }
            
            if (cnibFile.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
            }
            
            String originalFileName = cnibFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            
            if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                return ResponseEntity.badRequest().body("Format de fichier non autorisé. Formats acceptés : " + ALLOWED_EXTENSIONS);
            }
            
            // Générer un nom de fichier unique
            String fileName = generateUniqueFileName(originalFileName);
            String filePath = UPLOAD_DIR + fileName;
            
            // Sauvegarder le fichier
            Path path = Paths.get(filePath);
            Files.copy(cnibFile.getInputStream(), path);
            
            // Créer l'user
            User user = new User();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setPassword(password);
            user.setEmail(email);
            user.setCnib(fileName);
            user.setUserupdate("admin");
            user.setDateupdate(LocalDateTime.now());
            
            // Associer le role si fourni
            if (idrole != null) {
                Role role = roleRepository.findById(idrole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + idrole));
                user.setRole(role);
            }
            
         // Associer le pays si fourni
            if (idpays != null) {
                Pays pays = paysRepository.findById(idpays)
                    .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + idpays));
                user.setPays(pays);
            }
            
            // Sauvegarder l'user
            User savedUser = userRepository.save(user);
            UserDTO userDTO = UserDTO.fromEntity(savedUser);
            
            return ResponseEntity.ok(userDTO);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }
    
    // Mettre à jour un user (avec possibilité de changer la photo)
    @PutMapping(value = "/users/{iduser}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(
            @PathVariable Integer iduser,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) Integer idrole,
            @RequestParam(required = false) Integer idpays,
            @RequestParam(value = "cnib", required = false) MultipartFile cnibFile) {
        
        try {
            // Vérifier si l'user existe
            User user = userRepository.findById(iduser)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
            
            // Mettre à jour les champs
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setPassword(password);
            user.setEmail(email);
            
            // Gérer la nouvelle photo si fournie
            if (cnibFile != null && !cnibFile.isEmpty()) {
                // Validation du fichier
                if (cnibFile.getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.badRequest().body("La taille du fichier dépasse 5MB");
                }
                
                String originalFileName = cnibFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName);
                
                if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                    return ResponseEntity.badRequest().body("Format de fichier non autorisé. Formats acceptés : " + ALLOWED_EXTENSIONS);
                }
                
                // Supprimer l'ancienne photo si elle existe
                if (user.getCnib() != null) {
                    String oldFilePath = UPLOAD_DIR + user.getCnib();
                    File oldFile = new File(oldFilePath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                
                // Générer un nouveau nom de fichier et sauvegarder
                String fileName = generateUniqueFileName(originalFileName);
                String filePath = UPLOAD_DIR + fileName;
                Path path = Paths.get(filePath);
                Files.copy(cnibFile.getInputStream(), path);
                
                user.setCnib(fileName);
            }
            
            // Mettre à jour le role si fourni
            if (idrole != null) {
                Role role = roleRepository.findById(idrole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + idrole));
                user.setRole(role);
            } else {
                user.setRole(null);
            }
            
         // Mettre à jour le pays si fourni
            if (idpays != null) {
                Pays pays = paysRepository.findById(idpays)
                    .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + idpays));
                user.setPays(pays);
            } else {
                user.setPays(null);
            }
            
            // Sauvegarder les modifications
            User updatedUser = userRepository.save(user);
            UserDTO userDTO = UserDTO.fromEntity(updatedUser);
            
            return ResponseEntity.ok(userDTO);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }
    
    // Mettre à jour un user via JSON (sans changer la photo)
    @PutMapping("/users/{iduser}/json")
    public ResponseEntity<UserDTO> updateUserJson(
            @PathVariable Integer iduser,
            @Valid @RequestBody UserDTO userDTO) {
        
        User user = userRepository.findById(iduser)
            .orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
        
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        user.setUserupdate(userDTO.getUserupdate());
        
        if (userDTO.getIdrole() != null) {
            Role role = roleRepository.findById(userDTO.getIdrole())
                .orElseThrow(() -> new ResourceNotFoundException("Role non trouvé avec l'id : " + userDTO.getIdrole()));
            user.setRole(role);
        }
        
        if (userDTO.getIdpays() != null) {
            Pays pays = paysRepository.findById(userDTO.getIdpays())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id : " + userDTO.getIdpays()));
            user.setPays(pays);
        }
        
        User updatedUser = userRepository.save(user);
        UserDTO updatedDTO = UserDTO.fromEntity(updatedUser);
        
        return ResponseEntity.ok(updatedDTO);
    }
    
    // Supprimer un user
    @DeleteMapping("/users/{iduser}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Integer iduser) {
        User user = userRepository.findById(iduser)
            .orElseThrow(() -> new ResourceNotFoundException("User non trouvé avec l'id : " + iduser));
        
        // Supprimer la photo associée
        if (user.getCnib() != null) {
            String filePath = UPLOAD_DIR + user.getCnib();
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        
        userRepository.delete(user);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("supprimé", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
    
    // Télécharger la photo d'un user
    @GetMapping("/users/{iduser}/cnib")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Integer iduser) {
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
            
            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + user.getCnib() + "\"")
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