package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class TypeArticleDTO {
    
    private Integer idtype;
    
    @NotBlank(message = "Le libellé du type est obligatoire")
    @Size(min = 2, max = 100, message = "Le libellé doit contenir entre 2 et 100 caractères")
    private String libtype;
    
    @NotBlank(message = "La description du type est obligatoire")
    @Size(min = 5, max = 500, message = "La description doit contenir entre 5 et 500 caractères")
    private String desctype;
    
    private LocalDateTime dateupdate;
    private String userupdate;
    
    @NotNull(message = "La famille est obligatoire")
    private Integer idfamille;
    
    private String libfamille; // Pour afficher le nom de la famille
    
    // Constructeurs
    public TypeArticleDTO() {}
    
    public TypeArticleDTO(String libtype, String desctype, Integer idfamille) {
        this.libtype = libtype;
        this.desctype = desctype;
        this.idfamille = idfamille;
    }
    
    public TypeArticleDTO(Integer idtype, String libtype, String desctype, 
                         LocalDateTime dateupdate, String userupdate, Integer idfamille) {
        this.idtype = idtype;
        this.libtype = libtype;
        this.desctype = desctype;
        this.dateupdate = dateupdate;
        this.userupdate = userupdate;
        this.idfamille = idfamille;
    }
    
    // Méthodes de conversion
    public static TypeArticleDTO fromEntity(net.ecommerce.springboot.model.TypeArticle typeArticle) {
        TypeArticleDTO dto = new TypeArticleDTO();
        dto.setIdtype(typeArticle.getIdtype());
        dto.setLibtype(typeArticle.getLibtype());
        dto.setDesctype(typeArticle.getDesctype());
        dto.setDateupdate(typeArticle.getDateupdate());
        dto.setUserupdate(typeArticle.getUserupdate());
        
        // Informations de la famille
        if (typeArticle.getFamillearticle() != null) {
            dto.setIdfamille(typeArticle.getFamillearticle().getIdfamille());
            dto.setLibfamille(typeArticle.getFamillearticle().getLibfamille());
        }
        
        return dto;
    }
    
    public static net.ecommerce.springboot.model.TypeArticle toEntity(TypeArticleDTO dto) {
        net.ecommerce.springboot.model.TypeArticle typeArticle = new net.ecommerce.springboot.model.TypeArticle();
        typeArticle.setIdtype(dto.getIdtype());
        typeArticle.setLibtype(dto.getLibtype());
        typeArticle.setDesctype(dto.getDesctype());
        typeArticle.setUserupdate(dto.getUserupdate());
        typeArticle.setDateupdate(dto.getDateupdate());
        
        return typeArticle;
    }
    
    // Getters et Setters
    public Integer getIdtype() {
        return idtype;
    }
    
    public void setIdtype(Integer idtype) {
        this.idtype = idtype;
    }
    
    public String getLibtype() {
        return libtype;
    }
    
    public void setLibtype(String libtype) {
        this.libtype = libtype;
    }
    
    public String getDesctype() {
        return desctype;
    }
    
    public void setDesctype(String desctype) {
        this.desctype = desctype;
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
}