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
@Table(name = "TypeArticle")
public class TypeArticle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idtype;

	@Column(name = "libtype", nullable = false)
	private String libtype;

	@Column(name = "desctype", nullable = false)
	private String desctype;

	@Column(name = "dateupdate", nullable = false)
	private LocalDateTime dateupdate;

	@Column(name = "userupdate", nullable = false)
	private String userupdate;

	@PrePersist
	public void prePersist() {
		dateupdate = LocalDateTime.now();
//	        userupdate = "admin";
	}

	@PreUpdate
	public void preUpdate() {
		dateupdate = LocalDateTime.now();
//	        userupdate = "admin";
	}

	@ManyToOne
    @JoinColumn(name = "idfamille", nullable = true)
    private FamilleArticle familleArticle;

	public TypeArticle() {}

	public TypeArticle(int idtype, String libtype, String desctype, LocalDateTime dateupdate, String userupdate) {
		this.idtype = idtype;
		this.libtype = libtype;
		this.desctype = desctype;
		this.dateupdate = dateupdate;
		this.userupdate = userupdate;
	}

	public int getIdtype() {
		return idtype;
	}
	public void setIdtype(int idtype) {
		this.idtype=idtype;
	}
	public String getLibtype() {
		return libtype;
	}
	
	public void setLibtype(String libtype) {
		this.libtype=libtype;
	}
	
	public String getDesctype() {
		return desctype;
	}
	
	public void setDesctype(String desctype) {
		this.desctype=desctype;
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
	
	public FamilleArticle getFamillearticle() {
		return familleArticle;
	}
	
	public void setFamilleArticle(FamilleArticle familleArticle) {
		this.familleArticle=familleArticle;
	}
}
