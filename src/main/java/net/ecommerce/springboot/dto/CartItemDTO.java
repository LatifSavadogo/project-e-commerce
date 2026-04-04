package net.ecommerce.springboot.dto;

import net.ecommerce.springboot.model.CartItem;

public class CartItemDTO {

	private Integer idcartitem;
	private Integer idArticle;
	private String libelleArticle;
	private int quantity;
	private Integer prixUnitaireCatalogue;
	/** Non null si ligne liée à une négociation : prix unitaire accordé. */
	private Integer prixUnitaireNegocie;
	private Integer idMessageAccord;
	private boolean negociationVerrouillee;

	public static CartItemDTO fromEntity(CartItem ci) {
		CartItemDTO d = new CartItemDTO();
		d.setIdcartitem(ci.getIdcartitem());
		d.setIdArticle(ci.getArticle().getIdarticle());
		d.setLibelleArticle(ci.getArticle().getLibarticle());
		d.setQuantity(ci.getQuantity());
		d.setPrixUnitaireCatalogue(ci.getArticle().getPrixunitaire());
		if (ci.getAgreedMessage() != null) {
			d.setPrixUnitaireNegocie(ci.getAgreedMessage().getPrixPropose());
			d.setIdMessageAccord(ci.getAgreedMessage().getIdmessage());
			d.setNegociationVerrouillee(true);
		} else {
			d.setNegociationVerrouillee(false);
		}
		return d;
	}

	public Integer getIdcartitem() {
		return idcartitem;
	}

	public void setIdcartitem(Integer idcartitem) {
		this.idcartitem = idcartitem;
	}

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public String getLibelleArticle() {
		return libelleArticle;
	}

	public void setLibelleArticle(String libelleArticle) {
		this.libelleArticle = libelleArticle;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Integer getPrixUnitaireCatalogue() {
		return prixUnitaireCatalogue;
	}

	public void setPrixUnitaireCatalogue(Integer prixUnitaireCatalogue) {
		this.prixUnitaireCatalogue = prixUnitaireCatalogue;
	}

	public Integer getPrixUnitaireNegocie() {
		return prixUnitaireNegocie;
	}

	public void setPrixUnitaireNegocie(Integer prixUnitaireNegocie) {
		this.prixUnitaireNegocie = prixUnitaireNegocie;
	}

	public Integer getIdMessageAccord() {
		return idMessageAccord;
	}

	public void setIdMessageAccord(Integer idMessageAccord) {
		this.idMessageAccord = idMessageAccord;
	}

	public boolean isNegociationVerrouillee() {
		return negociationVerrouillee;
	}

	public void setNegociationVerrouillee(boolean negociationVerrouillee) {
		this.negociationVerrouillee = negociationVerrouillee;
	}
}
