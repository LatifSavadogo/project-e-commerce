package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "RoleUpgradeRequest")
public class RoleUpgradeRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "iduser", nullable = false)
	private User user;

	/** {@link net.ecommerce.springboot.security.RoleNames#VENDEUR} ou {@link net.ecommerce.springboot.security.RoleNames#LIVREUR} */
	@Column(name = "role_demande", nullable = false, length = 32)
	private String roleDemande;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private RoleUpgradeStatus status = RoleUpgradeStatus.PENDING;

	@Column(name = "cnib_filename", nullable = false, length = 512)
	private String cnibFilename;

	@Column(name = "photo_filename", nullable = false, length = 512)
	private String photoFilename;

	private Double latitude;

	private Double longitude;

	@Column(name = "idtype_vendeur")
	private Integer idtypeVendeur;

	@Enumerated(EnumType.STRING)
	@Column(name = "type_engin_livreur", length = 16)
	private TypeEnginLivreur typeEnginLivreur;

	@Column(name = "admin_motif", length = 1000)
	private String adminMotif;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime decidedAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRoleDemande() {
		return roleDemande;
	}

	public void setRoleDemande(String roleDemande) {
		this.roleDemande = roleDemande;
	}

	public RoleUpgradeStatus getStatus() {
		return status;
	}

	public void setStatus(RoleUpgradeStatus status) {
		this.status = status;
	}

	public String getCnibFilename() {
		return cnibFilename;
	}

	public void setCnibFilename(String cnibFilename) {
		this.cnibFilename = cnibFilename;
	}

	public String getPhotoFilename() {
		return photoFilename;
	}

	public void setPhotoFilename(String photoFilename) {
		this.photoFilename = photoFilename;
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

	public TypeEnginLivreur getTypeEnginLivreur() {
		return typeEnginLivreur;
	}

	public void setTypeEnginLivreur(TypeEnginLivreur typeEnginLivreur) {
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
}
