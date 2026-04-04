package net.ecommerce.springboot.dto;

public class VendorTopArticleDTO {

	private Integer idArticle;
	private String libelle;
	private long revenue;
	private long quantitySold;

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public long getRevenue() {
		return revenue;
	}

	public void setRevenue(long revenue) {
		this.revenue = revenue;
	}

	public long getQuantitySold() {
		return quantitySold;
	}

	public void setQuantitySold(long quantitySold) {
		this.quantitySold = quantitySold;
	}
}
