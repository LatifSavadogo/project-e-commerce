package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;
import java.util.List;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDTO {

private Integer iduser;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(max = 50, message = "Le nom d'utilisateur ne doit pas dépasser 50 caractères")
    private String nom;
    
    @NotBlank(message = "Le prenom d'utilisateur est obligatoire")
    @Size(max = 50, message = "Le prenom d'utilisateur ne doit pas dépasser 50 caractères")
    private String prenom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
    
    private String userupdate;
    
    private LocalDateTime dateupdate;
    
    private String cnib;
    
    
    // Relations
    @NotNull(message = "Le rôle est obligatoire")
    private Integer idrole;
    
    private String librole;
    
    private Integer idpays;
    
    private String libpays;

    private String ville;

    /** Type d'article autorisé pour un vendeur (null si acheteur / admin). */
    private Integer idtypeVendeur;
    private String libtypeVendeur;

    /** MOTO ou VEHICULE pour les livreurs. */
    private String typeEnginLivreur;

    /** Marché international (vendeur). Absent en JSON = pas de changement pour les mises à jour partielles. */
    private Boolean vendeurInternational;

    /** Fin d’abonnement « vendeur certifié » (null si jamais souscrit ou expiré). */
    private LocalDateTime vendeurCertifieJusqua;
    private Boolean vendeurCertifieActif;

    private String photoProfil;

    private Double latitude;

    private Double longitude;
    
    
    // Constructeurs
    public UserDTO() {}
    
    // Constructeur pour création
    public UserDTO(String nom, String prenom, String email, String cnib, String password, Integer idrole, Integer idpays) {
        this.nom = nom;
        this.prenom=prenom;
        this.email = email;
        this.password = password;
        this.idrole = idrole;
        this.cnib=cnib;
    }
    
    public static UserDTO fromEntity(net.ecommerce.springboot.model.User user) {
        UserDTO dto = new UserDTO();
        dto.setIduser(user.getIduser());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setCnib(user.getCnib());
        dto.setPhotoProfil(user.getPhotoProfil());
        dto.setLatitude(user.getLatitude());
        dto.setLongitude(user.getLongitude());
        // On ne retourne pas le mot de passe pour des raisons de sécurité
        dto.setUserupdate(user.getUserupdate());
        dto.setDateupdate(user.getDateupdate());
        
        // Gestion du rôle
        if (user.getRole() != null) {
            dto.setIdrole(user.getRole().getIdrole());
            dto.setLibrole(user.getRole().getLibrole());
        }
        
        if (user.getPays() != null) {
            dto.setIdpays(user.getPays().getIdpays());
            dto.setLibpays(user.getPays().getLibpays());
        }
        dto.setVille(user.getVille());
        if (user.getCategorieVendeur() != null) {
            dto.setIdtypeVendeur(user.getCategorieVendeur().getIdtype());
            dto.setLibtypeVendeur(user.getCategorieVendeur().getLibtype());
        }
        if (user.getTypeEnginLivreur() != null) {
            dto.setTypeEnginLivreur(user.getTypeEnginLivreur().name());
        }
        dto.setVendeurInternational(user.isVendeurInternational());
        dto.setVendeurCertifieJusqua(user.getVendeurCertifieJusqua());
        LocalDateTime until = user.getVendeurCertifieJusqua();
        dto.setVendeurCertifieActif(until != null && until.isAfter(LocalDateTime.now()));
        
        return dto;
    }
    
    // Getters et Setters
    public Integer getIduser() { return iduser; }
    public void setIduser(Integer iduser) { this.iduser = iduser; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    

    public String getCnib() { return cnib; }
    public void setCnib(String cnib) { this.cnib = cnib; }
    
    public String getUserupdate() { return userupdate; }
    public void setUserupdate(String userupdate) { this.userupdate = userupdate; }
    
    public LocalDateTime getDateupdate() { return dateupdate; }
    public void setDateupdate(LocalDateTime dateupdate) { this.dateupdate = dateupdate; }
    
    public Integer getIdrole() { return idrole; }
    public void setIdrole(Integer idrole) { this.idrole = idrole; }
    
    public String getLibrole() { return librole; }
    public void setLibrole(String librole) { this.librole = librole; }
    
    public Integer getIdpays() { return idpays; }
    public void setIdpays(Integer idpays) { this.idpays = idpays; }
    
    public String getLibpays() { return libpays; }
    public void setLibpays(String libpays) { this.libpays = libpays; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public Integer getIdtypeVendeur() { return idtypeVendeur; }
    public void setIdtypeVendeur(Integer idtypeVendeur) { this.idtypeVendeur = idtypeVendeur; }

    public String getLibtypeVendeur() { return libtypeVendeur; }
    public void setLibtypeVendeur(String libtypeVendeur) { this.libtypeVendeur = libtypeVendeur; }

    public String getTypeEnginLivreur() { return typeEnginLivreur; }
    public void setTypeEnginLivreur(String typeEnginLivreur) { this.typeEnginLivreur = typeEnginLivreur; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getVendeurInternational() {
        return vendeurInternational;
    }

    public void setVendeurInternational(Boolean vendeurInternational) {
        this.vendeurInternational = vendeurInternational;
    }

    public LocalDateTime getVendeurCertifieJusqua() {
        return vendeurCertifieJusqua;
    }

    public void setVendeurCertifieJusqua(LocalDateTime vendeurCertifieJusqua) {
        this.vendeurCertifieJusqua = vendeurCertifieJusqua;
    }

    public Boolean getVendeurCertifieActif() {
        return vendeurCertifieActif;
    }

    public void setVendeurCertifieActif(Boolean vendeurCertifieActif) {
        this.vendeurCertifieActif = vendeurCertifieActif;
    }
  
}
