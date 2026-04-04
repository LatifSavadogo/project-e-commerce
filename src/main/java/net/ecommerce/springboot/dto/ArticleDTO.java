package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class ArticleDTO {
    
    private Integer idarticle;
    
    @NotBlank(message = "Le libellé de l'article est obligatoire")
    private String libarticle;
    
    @NotBlank(message = "La photo est obligatoire")
    private String photo;
    
    @NotBlank(message = "La description de l'article est obligatoire")
    private String descarticle;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private Integer prixunitaire;
    
    private LocalDateTime dateupdate;
    private String userupdate;
    private Integer idtype;
    private String typearticle; // nom du type
    private String desctype; // description du type
    
    public ArticleDTO() {}
    
    public ArticleDTO(String libarticle, String photo, String descarticle, 
                     Integer prixunitaire, LocalDateTime dateupdate, String userupdate) {
        this.libarticle = libarticle;
        this.photo = photo;
        this.descarticle = descarticle;
        this.prixunitaire = prixunitaire;
        this.userupdate = userupdate;
        this.dateupdate = dateupdate;
    }
    
    public static ArticleDTO fromEntity(net.ecommerce.springboot.model.Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setIdarticle(article.getIdarticle());
        dto.setLibarticle(article.getLibarticle());
        dto.setPhoto(article.getPhoto());
        dto.setDescarticle(article.getDescarticle());
        dto.setPrixunitaire(article.getPrixunitaire());
        dto.setDateupdate(article.getDateupdate());
        dto.setUserupdate(article.getUserupdate());
        
        // Gestion du type d'article
        if (article.getTypeArticle() != null) {
            dto.setIdtype(article.getTypeArticle().getIdtype());
            dto.setTypearticle(article.getTypeArticle().getLibtype());
        }
        return dto;
    }
    
    public static net.ecommerce.springboot.model.Article toEntity(ArticleDTO dto) {
        net.ecommerce.springboot.model.Article article = new net.ecommerce.springboot.model.Article();
        article.setIdarticle(dto.getIdarticle());
        article.setLibarticle(dto.getLibarticle());
        article.setPhoto(dto.getPhoto());
        article.setDescarticle(dto.getDescarticle());
        article.setPrixunitaire(dto.getPrixunitaire());
        article.setUserupdate(dto.getUserupdate());
        article.setDateupdate(dto.getDateupdate());
        
        return article;
    }
    
    // Getters et Setters
    public Integer getIdarticle() { 
        return idarticle; 
    }
    
    public void setIdarticle(Integer idarticle) { 
        this.idarticle = idarticle; 
    }
    
    public String getLibarticle() { 
        return libarticle; 
    }
    
    public void setLibarticle(String libarticle) { 
        this.libarticle = libarticle; 
    }
    
    public String getDescarticle() { 
        return descarticle; 
    }
    
    public void setDescarticle(String descarticle) { 
        this.descarticle = descarticle; 
    }
    
    public String getPhoto() { 
        return photo; 
    }
    
    public void setPhoto(String photo) { 
        this.photo = photo; 
    }
    
    public Integer getPrixunitaire() { 
        return prixunitaire; 
    }
    
    public void setPrixunitaire(Integer prixunitaire) { 
        this.prixunitaire = prixunitaire; 
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

    public Integer getIdtype() { 
        return idtype; 
    }

    public void setIdtype(Integer idtype) { 
        this.idtype = idtype; 
    }

    public String getTypearticle() {
        return typearticle;
    }

    public void setTypearticle(String typearticle) {
        this.typearticle = typearticle;
    }

    public String getDesctype() {
        return desctype;
    }

    public void setDesctype(String desctype) {
        this.desctype = desctype;
    }
}