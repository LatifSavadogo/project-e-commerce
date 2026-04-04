package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FamilleArticleDTO {
    
    private Integer idfamille;
    
    @NotBlank(message = "Le libellé de la famille est obligatoire")
    @Size(min = 2, max = 100, message = "Le libellé doit contenir entre 2 et 100 caractères")
    private String libfamille;
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 500, message = "La description doit contenir entre 5 et 500 caractères")
    private String description;
    
    private LocalDateTime dateupdate;
    
    private String userupdate;
    
    private List<TypeArticleDTO> typeArticles;
    
    // Constructeurs
    public FamilleArticleDTO() {}
    
    public FamilleArticleDTO(String libfamille, String description, 
                            LocalDateTime dateupdate, String userupdate) {
        this.libfamille = libfamille;
        this.description = description;
        this.dateupdate = dateupdate;
        this.userupdate = userupdate;
    }
    
    public FamilleArticleDTO(Integer idfamille, String libfamille, String description,
                            LocalDateTime dateupdate, String userupdate) {
        this.idfamille = idfamille;
        this.libfamille = libfamille;
        this.description = description;
        this.dateupdate = dateupdate;
        this.userupdate = userupdate;
    }
    
    // Méthodes de conversion
    public static FamilleArticleDTO fromEntity(net.ecommerce.springboot.model.FamilleArticle familleArticle) {
        FamilleArticleDTO dto = new FamilleArticleDTO();
        dto.setIdfamille(familleArticle.getIdfamille());
        dto.setLibfamille(familleArticle.getLibfamille());
        dto.setDescription(familleArticle.getDescription());
        dto.setDateupdate(familleArticle.getDateupdate());
        dto.setUserupdate(familleArticle.getUserupdate());
        
        // Conversion des TypeArticle associés
        if (familleArticle.getTypeArticles() != null) {
            List<TypeArticleDTO> typeArticleDTOs = familleArticle.getTypeArticles()
                .stream()
                .map(TypeArticleDTO::fromEntity)
                .collect(Collectors.toList());
            dto.setTypeArticles(typeArticleDTOs);
        }
        
        return dto;
    }
    
    public static net.ecommerce.springboot.model.FamilleArticle toEntity(FamilleArticleDTO dto) {
        net.ecommerce.springboot.model.FamilleArticle familleArticle = new net.ecommerce.springboot.model.FamilleArticle();
        familleArticle.setIdfamille(dto.getIdfamille());
        familleArticle.setLibfamille(dto.getLibfamille());
        familleArticle.setDescription(dto.getDescription());
        familleArticle.setUserupdate(dto.getUserupdate());
        familleArticle.setDateupdate(dto.getDateupdate());
        
        return familleArticle;
    }
    
    // Getters et Setters
    public Integer getIdfamille() {
        return idfamille;
    }
    
    public void setIdfamille(Integer idfamille) {
        this.idfamille = idfamille;
    }
    
    public String getLibfamille() {
        return libfamille;
    }
    
    public void setLibfamille(String libfamille) {
        this.libfamille = libfamille;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getDateupdate() {
        return dateupdate;
    }
    
    public void setDateupdate(LocalDateTime dateupdate) {
        this.dateupdate = dateupdate;
    }
    
    public String getUserupdate() {
        return userupdate;
    }
    
    public void setUserupdate(String userupdate) {
        this.userupdate = userupdate;
    }
    
    public List<TypeArticleDTO> getTypeArticles() {
        return typeArticles;
    }
    
    public void setTypeArticles(List<TypeArticleDTO> typeArticles) {
        this.typeArticles = typeArticles;
    }
}