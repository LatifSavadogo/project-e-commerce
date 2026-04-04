package net.ecommerce.springboot.controller;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.TypeArticleDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.FamilleArticle;
import net.ecommerce.springboot.model.TypeArticle;
import net.ecommerce.springboot.repository.FamilleArticleRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/")
public class TypeArticleController {

    @Autowired
    private TypeArticleRepository typeArticleRepository;

    @Autowired
    private FamilleArticleRepository familleArticleRepository;
    

    @GetMapping("/typeArticles")
    public List<TypeArticleDTO> getAllTypeArticles() {
        return typeArticleRepository.findAll()
                .stream()
                .map(TypeArticleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/typeArticles/{idtype}")
    public ResponseEntity<TypeArticleDTO> getTypeArticleById(@PathVariable Integer idtype) {
    	TypeArticle typeArticle = typeArticleRepository.findById(idtype).orElseThrow(
                () -> new ResourceNotFoundException("Le type d'article avec l'identifiant " + idtype + " n'existe pas"));
        return ResponseEntity.ok(TypeArticleDTO.fromEntity(typeArticle));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/typeArticles")
    public ResponseEntity<TypeArticleDTO> createTypeArticle(@Valid @RequestBody TypeArticleDTO typeArticleDTO) {
        TypeArticle typeArticle = new TypeArticle();
        typeArticle.setDesctype(typeArticleDTO.getDesctype());
        typeArticle.setLibtype(typeArticleDTO.getLibtype());
//        typeArticle.setMontant(typeArticleDTO.getMontant());
//        typeArticle.setRefpaie(typeArticleDTO.getRefpaie());
        typeArticle.setUserupdate("admin");
        
        FamilleArticle familleArticle = familleArticleRepository.findById(typeArticleDTO.getIdfamille())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facture introuvable avec id: " + typeArticleDTO.getIdfamille()));
        typeArticle.setFamilleArticle(familleArticle);

        TypeArticle savedTypeArticle = typeArticleRepository.save(typeArticle);
        return ResponseEntity.status(HttpStatus.CREATED).body(TypeArticleDTO.fromEntity(savedTypeArticle));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/typeArticles/{idtype}")
    public ResponseEntity<TypeArticleDTO> updateTypeArticleById(@PathVariable Integer idtype,
            @Valid @RequestBody TypeArticleDTO typeArticleDTO) {
//    	String currentUsername = authService.getCurrentUsername();
    	TypeArticle typeArticle = typeArticleRepository.findById(idtype)
                .orElseThrow(() -> new ResourceNotFoundException("Le type d'article avec l'id " + idtype + " n'existe pas"));
        
        
        typeArticle.setDesctype(typeArticleDTO.getDesctype());
        typeArticle.setLibtype(typeArticleDTO.getLibtype());
//        typeArticle.setMontant(typeArticleDTO.getMontant());
//        typeArticle.setRefpaie(typeArticleDTO.getRefpaie());
        typeArticle.setUserupdate("admin");
        
        // Mise à jour de la famille d'article
        if (typeArticleDTO.getIdfamille() != null) {
        	FamilleArticle familleArticle = familleArticleRepository.findById(typeArticleDTO.getIdfamille())
                    .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec id: " + typeArticleDTO.getIdfamille()));
        	typeArticle.setFamilleArticle(familleArticle);
        }

        TypeArticle updateTypeArticle = typeArticleRepository.save(typeArticle);
        return ResponseEntity.ok(TypeArticleDTO.fromEntity(updateTypeArticle));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/typeArticles/{idtype}")
    public ResponseEntity<Map<String, Boolean>> deleteTypeArticle(@PathVariable Integer idtype) {
    	TypeArticle typeArticle = typeArticleRepository.findById(idtype)
                .orElseThrow(() -> new ResourceNotFoundException("Le type d'article avec l'id " + idtype + " n'existe pas"));
    	typeArticleRepository.delete(typeArticle);
        Map<String, Boolean> response = new HashMap<>();
        response.put("supprimé", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}