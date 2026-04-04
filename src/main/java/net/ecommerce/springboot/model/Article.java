package net.ecommerce.springboot.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="Article")
public class Article {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer idarticle;
	
	@Column(name="libarticle", nullable=false)
	private String libarticle;
	
	@Column(name="photo", nullable=false)
	private String photo;
	
	@Column(name="descarticle", nullable=false)
	private String descarticle;
	
	@Column(name="prixunitaire", nullable=false)
	private int prixunitaire;
	
	@Column(name="userupdate", nullable=false)
	private String userupdate;
	
	@Column(name="dateupdate", nullable=false)
	private LocalDateTime dateupdate;
	
	@ManyToOne
	@JoinColumn(name = "idtype", nullable = true)
	private TypeArticle typeArticle;

	@ManyToOne
	@JoinColumn(name = "iduser_vendeur", nullable = true)
	private User vendeur;

	@Column(nullable = false)
	private boolean blocked = false;

	@Column(length = 500)
	private String warningMessage;

	@Column(nullable = false)
	private int viewCount = 0;
	
	@PrePersist
    public void prePersist() {
        dateupdate = LocalDateTime.now();
        userupdate = "admin";
    }

    @PreUpdate
    public void preUpdate() {
        dateupdate = LocalDateTime.now();
        userupdate = "admin";
    }
	
	public Article() {	}
	
	private Article(String libarticle, String descarticle, int prixunitaire, 
			String userupdate, LocalDateTime dateupdate) {
		this.libarticle=libarticle;
		this.descarticle=descarticle;
		this.prixunitaire=prixunitaire;
		this.userupdate=userupdate;
	}

	public void setTypeArticle(TypeArticle typeArticle) {
		this.typeArticle=typeArticle;
	}
	
	public TypeArticle getTypeArticle() {
		return typeArticle;
	}
	
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getDescarticle() {
		return descarticle;
	}

	public void setDescarticle(String descarticle) {
		this.descarticle = descarticle;
	}

	public int getPrixunitaire() {
		return prixunitaire;
	}

	public void setPrixunitaire(int prixunitaire) {
		this.prixunitaire = prixunitaire;
	}

	public String getUserupdate() {
		return userupdate;
	}

	public void setUserupdate(String userupdate) {
		this.userupdate = userupdate;
	}

	public LocalDateTime getDateupdate() {
		return dateupdate;
	}

	public void setDateupdate(LocalDateTime dateupdate) {
		this.dateupdate = dateupdate;
	}

	public User getVendeur() {
		return vendeur;
	}

	public void setVendeur(User vendeur) {
		this.vendeur = vendeur;
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

}
