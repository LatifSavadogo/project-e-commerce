package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

import net.ecommerce.springboot.model.Conversation;

public class ConversationDTO {

	private Integer idconversation;
	private Integer idAcheteur;
	private String acheteurNom;
	private Integer idVendeur;
	private String vendeurNom;
	private Integer idArticle;
	private String articleLibelle;
	private LocalDateTime datecreation;
	private LocalDateTime dateupdate;

	public static ConversationDTO fromEntity(Conversation c) {
		ConversationDTO d = new ConversationDTO();
		d.setIdconversation(c.getIdconversation());
		if (c.getAcheteur() != null) {
			d.setIdAcheteur(c.getAcheteur().getIduser());
			d.setAcheteurNom(c.getAcheteur().getNom() + " " + c.getAcheteur().getPrenom());
		}
		if (c.getVendeur() != null) {
			d.setIdVendeur(c.getVendeur().getIduser());
			d.setVendeurNom(c.getVendeur().getNom() + " " + c.getVendeur().getPrenom());
		}
		if (c.getArticle() != null) {
			d.setIdArticle(c.getArticle().getIdarticle());
			d.setArticleLibelle(c.getArticle().getLibarticle());
		}
		d.setDatecreation(c.getDatecreation());
		d.setDateupdate(c.getDateupdate());
		return d;
	}

	public Integer getIdconversation() {
		return idconversation;
	}

	public void setIdconversation(Integer idconversation) {
		this.idconversation = idconversation;
	}

	public Integer getIdAcheteur() {
		return idAcheteur;
	}

	public void setIdAcheteur(Integer idAcheteur) {
		this.idAcheteur = idAcheteur;
	}

	public String getAcheteurNom() {
		return acheteurNom;
	}

	public void setAcheteurNom(String acheteurNom) {
		this.acheteurNom = acheteurNom;
	}

	public Integer getIdVendeur() {
		return idVendeur;
	}

	public void setIdVendeur(Integer idVendeur) {
		this.idVendeur = idVendeur;
	}

	public String getVendeurNom() {
		return vendeurNom;
	}

	public void setVendeurNom(String vendeurNom) {
		this.vendeurNom = vendeurNom;
	}

	public Integer getIdArticle() {
		return idArticle;
	}

	public void setIdArticle(Integer idArticle) {
		this.idArticle = idArticle;
	}

	public String getArticleLibelle() {
		return articleLibelle;
	}

	public void setArticleLibelle(String articleLibelle) {
		this.articleLibelle = articleLibelle;
	}

	public LocalDateTime getDatecreation() {
		return datecreation;
	}

	public void setDatecreation(LocalDateTime datecreation) {
		this.datecreation = datecreation;
	}

	public LocalDateTime getDateupdate() {
		return dateupdate;
	}

	public void setDateupdate(LocalDateTime dateupdate) {
		this.dateupdate = dateupdate;
	}
}
