package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import net.ecommerce.springboot.model.ArticleImage;

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

    private Integer idVendeur;
    private String vendeurNom;
    private String vendeurPrenom;
    /** Présent si vendeur connu : annonces du marché international. */
    private Boolean vendeurInternational;
    /** Moyenne des étoiles (1–5) ; null si aucun avis. */
    private Double vendeurNoteMoyenne;
    private int vendeurNombreAvis;
    /** Badge vendeur certifié (abonnement actif). */
    private boolean vendeurCertifieActif;
    private boolean blocked;
    private String warningMessage;
    private int viewCount;

    /** Fichiers image (noms stockés), ordre d’affichage ; la première = photo principale. */
    private List<String> photos = new ArrayList<>();
    
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
        return fromEntity(article, List.of());
    }

    public static ArticleDTO fromEntity(net.ecommerce.springboot.model.Article article, List<ArticleImage> images) {
        ArticleDTO dto = new ArticleDTO();
        dto.setIdarticle(article.getIdarticle());
        dto.setLibarticle(article.getLibarticle());
        dto.setDescarticle(article.getDescarticle());
        dto.setPrixunitaire(article.getPrixunitaire());
        dto.setDateupdate(article.getDateupdate());
        dto.setUserupdate(article.getUserupdate());

        List<String> photoList = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (ArticleImage img : images) {
                photoList.add(img.getUrl());
            }
            dto.setPhoto(photoList.get(0));
        } else if (article.getPhoto() != null && !article.getPhoto().isBlank()) {
            dto.setPhoto(article.getPhoto());
            photoList.add(article.getPhoto());
        } else {
            dto.setPhoto(null);
        }
        dto.setPhotos(photoList);

        if (article.getTypeArticle() != null) {
            dto.setIdtype(article.getTypeArticle().getIdtype());
            dto.setTypearticle(article.getTypeArticle().getLibtype());
            dto.setDesctype(article.getTypeArticle().getDesctype());
        }
        if (article.getVendeur() != null) {
            dto.setIdVendeur(article.getVendeur().getIduser());
            dto.setVendeurNom(article.getVendeur().getNom());
            dto.setVendeurPrenom(article.getVendeur().getPrenom());
            dto.setVendeurInternational(article.getVendeur().isVendeurInternational());
            dto.setVendeurCertifieActif(article.getVendeur().getVendeurCertifieJusqua() != null
                    && article.getVendeur().getVendeurCertifieJusqua().isAfter(java.time.LocalDateTime.now()));
        }
        dto.setBlocked(article.isBlocked());
        dto.setWarningMessage(article.getWarningMessage());
        dto.setViewCount(article.getViewCount());
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

    public Integer getIdVendeur() {
        return idVendeur;
    }

    public void setIdVendeur(Integer idVendeur) {
        this.idVendeur = idVendeur;
    }

    public String getVendeurNom() {
        return vendeurNom;
    }

    public void setVendeurNom(String vendeurNom) {
        this.vendeurNom = vendeurNom;
    }

    public String getVendeurPrenom() {
        return vendeurPrenom;
    }

    public void setVendeurPrenom(String vendeurPrenom) {
        this.vendeurPrenom = vendeurPrenom;
    }

    public Boolean getVendeurInternational() {
        return vendeurInternational;
    }

    public void setVendeurInternational(Boolean vendeurInternational) {
        this.vendeurInternational = vendeurInternational;
    }

    public Double getVendeurNoteMoyenne() {
        return vendeurNoteMoyenne;
    }

    public void setVendeurNoteMoyenne(Double vendeurNoteMoyenne) {
        this.vendeurNoteMoyenne = vendeurNoteMoyenne;
    }

    public int getVendeurNombreAvis() {
        return vendeurNombreAvis;
    }

    public void setVendeurNombreAvis(int vendeurNombreAvis) {
        this.vendeurNombreAvis = vendeurNombreAvis;
    }

    public boolean isVendeurCertifieActif() {
        return vendeurCertifieActif;
    }

    public void setVendeurCertifieActif(boolean vendeurCertifieActif) {
        this.vendeurCertifieActif = vendeurCertifieActif;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos != null ? photos : new ArrayList<>();
    }
}