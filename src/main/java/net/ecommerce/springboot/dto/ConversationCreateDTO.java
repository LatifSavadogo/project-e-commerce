package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationCreateDTO {

	@NotNull
	private Integer idVendeur;

	private Integer idArticle;

	public Integer getIdVendeur() {
		return idVendeur;
	}

	public void setIdVendeur(Integer idVendeur) {
		this.idVendeur = idVendeur;
	}

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}
}
