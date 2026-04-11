package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

import net.ecommerce.springboot.model.RoleUpgradeRequest;
import net.ecommerce.springboot.model.RoleUpgradeStatus;

public class RoleUpgradeRequestDTO {

	private Integer id;
	private Integer iduser;
	private String emailDemandeur;
	private String roleDemande;
	private String status;
	private Double latitude;
	private Double longitude;
	private Integer idtypeVendeur;
	private String typeEnginLivreur;
	private String adminMotif;
	private LocalDateTime createdAt;
	private LocalDateTime decidedAt;

	/** Noms fichiers côté serveur (dossier pending tant que la demande est en attente). */
	private String fichierCnib;
	private String fichierPhoto;

	public static RoleUpgradeRequestDTO fromEntity(RoleUpgradeRequest e) {
		RoleUpgradeRequestDTO d = new RoleUpgradeRequestDTO();
		d.setId(e.getId());
		if (e.getUser() != null) {
			d.setIduser(e.getUser().getIduser());
			d.setEmailDemandeur(e.getUser().getEmail());
		}
		d.setRoleDemande(e.getRoleDemande());
		RoleUpgradeStatus st = e.getStatus();
		d.setStatus(st != null ? st.name() : null);
		d.setLatitude(e.getLatitude());
		d.setLongitude(e.getLongitude());
		d.setIdtypeVendeur(e.getIdtypeVendeur());
		if (e.getTypeEnginLivreur() != null) {
			d.setTypeEnginLivreur(e.getTypeEnginLivreur().name());
		}
		d.setAdminMotif(e.getAdminMotif());
		d.setCreatedAt(e.getCreatedAt());
		d.setDecidedAt(e.getDecidedAt());
		d.setFichierCnib(e.getCnibFilename());
		d.setFichierPhoto(e.getPhotoFilename());
		return d;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIduser() {
		return iduser;
	}

	public void setIduser(Integer iduser) {
		this.iduser = iduser;
	}

	public String getEmailDemandeur() {
		return emailDemandeur;
	}

	public void setEmailDemandeur(String emailDemandeur) {
		this.emailDemandeur = emailDemandeur;
	}

	public String getRoleDemande() {
		return roleDemande;
	}

	public void setRoleDemande(String roleDemande) {
		this.roleDemande = roleDemande;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Integer getIdtypeVendeur() {
		return idtypeVendeur;
	}

	public void setIdtypeVendeur(Integer idtypeVendeur) {
		this.idtypeVendeur = idtypeVendeur;
	}

	public String getTypeEnginLivreur() {
		return typeEnginLivreur;
	}

	public void setTypeEnginLivreur(String typeEnginLivreur) {
		this.typeEnginLivreur = typeEnginLivreur;
	}

	public String getAdminMotif() {
		return adminMotif;
	}

	public void setAdminMotif(String adminMotif) {
		this.adminMotif = adminMotif;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getDecidedAt() {
		return decidedAt;
	}

	public void setDecidedAt(LocalDateTime decidedAt) {
		this.decidedAt = decidedAt;
	}

	public String getFichierCnib() {
		return fichierCnib;
	}

	public void setFichierCnib(String fichierCnib) {
		this.fichierCnib = fichierCnib;
	}

	public String getFichierPhoto() {
		return fichierPhoto;
	}

	public void setFichierPhoto(String fichierPhoto) {
		this.fichierPhoto = fichierPhoto;
	}
}
