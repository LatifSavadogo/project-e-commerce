package net.ecommerce.springboot.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "FamilleArticle")
public class FamilleArticle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idfamille;

	@Column(name = "libfamille", nullable = false)
	private String libfamille;

	@Column(name = "description", nullable = false)
	private String description;

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

	@OneToMany(mappedBy = "familleArticle")
	private List<TypeArticle> typeArticles;

	public FamilleArticle() {
	}

	public FamilleArticle(String libfamille, String description, LocalDateTime dateupdate, String userupdate,
			List<TypeArticle> typeArticles) {
		super();
		this.idfamille = idfamille;
		this.libfamille = libfamille;
		this.description = description;
		this.dateupdate = dateupdate;
		this.userupdate = userupdate;
		this.typeArticles = typeArticles;
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

	public List<TypeArticle> getTypeArticles() {
		return typeArticles;
	}

	public void setTypeArticles(List<TypeArticle> typeArticles) {
		this.typeArticles = typeArticles;
	}

	public TypeArticle addTypeArticle(TypeArticle typeArticle) {
		getTypeArticles().add(typeArticle);
		typeArticle.setFamilleArticle(this);

		return typeArticle;
	}

	public TypeArticle removeTypeArticle(TypeArticle typeArticle) {
		getTypeArticles().remove(typeArticle);
		typeArticle.setFamilleArticle(null);

		return typeArticle;
	}
}
