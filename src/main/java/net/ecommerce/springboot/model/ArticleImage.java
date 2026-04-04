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
@Table(name="`ArticleImage`")
public class ArticleImage {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer idArticleImage;
	
	@Column(name="url", nullable=false)
	private String url;
	
	@Column(name="isPrimary", nullable=false)
	private Boolean isPrimary;
	
	@Column(name="sortOrder", nullable=false)
	private Integer sortOrder;
	
	@Column(name="uploadedAt", nullable=false)
	private LocalDateTime  uploadedAt;
	

	@Column(name="createAt", nullable=false)
	private LocalDateTime createAt;
	

	@Column(name="updateAt", nullable=false)
	private LocalDateTime updateAt;
	
	@PrePersist
    public void prePersist() {
    	createAt = LocalDateTime.now();
    	updateAt =  LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateAt = LocalDateTime.now(); 
    }
    
    @ManyToOne
   	@JoinColumn(name = "idarticle")
   	private Article article;

    public ArticleImage() {}
	public ArticleImage(Integer idArticleImage, String url, Boolean isPrimary, Integer sortOrder, LocalDateTime uploadedAt,
			LocalDateTime createAt, LocalDateTime updateAt, Article article) {
		super();
		this.idArticleImage = idArticleImage;
		this.url = url;
		this.isPrimary = isPrimary;
		this.sortOrder = sortOrder;
		this.uploadedAt = uploadedAt;
		this.createAt = createAt;
		this.updateAt = updateAt;
		this.article = article;
	}

	public Integer getIdArticleImage() {
		return idArticleImage;
	}

	public void setIdArticleImage(Integer idArticleImage) {
		this.idArticleImage = idArticleImage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIsPrimary() {
		return isPrimary;
	}

	public void setIsPrimary(Boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public LocalDateTime getCreateAt() {
		return createAt;
	}

	public void setCreateAt(LocalDateTime createAt) {
		this.createAt = createAt;
	}

	public LocalDateTime getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(LocalDateTime updateAt) {
		this.updateAt = updateAt;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}
}
