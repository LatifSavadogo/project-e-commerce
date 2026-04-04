package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

import net.ecommerce.springboot.model.ChatMessage;

public class ChatMessageDTO {

	private Integer idmessage;
	private Integer idconversation;
	private Integer idAuteur;
	private String auteurNom;
	private String contenu;
	private LocalDateTime dateenvoi;
	private Integer prixPropose;
	private Integer quantiteProposee;
	private String statutOffre;
	private Boolean offreFinaleVendeur;

	public static ChatMessageDTO fromEntity(ChatMessage m) {
		ChatMessageDTO d = new ChatMessageDTO();
		d.setIdmessage(m.getIdmessage());
		if (m.getConversation() != null) {
			d.setIdconversation(m.getConversation().getIdconversation());
		}
		if (m.getAuteur() != null) {
			d.setIdAuteur(m.getAuteur().getIduser());
			d.setAuteurNom(m.getAuteur().getNom() + " " + m.getAuteur().getPrenom());
		}
		d.setContenu(m.getContenu());
		d.setDateenvoi(m.getDateenvoi());
		d.setPrixPropose(m.getPrixPropose());
		d.setQuantiteProposee(m.getQuantiteProposee());
		if (m.getStatutOffre() != null) {
			d.setStatutOffre(m.getStatutOffre().name());
		}
		d.setOffreFinaleVendeur(m.getOffreFinaleVendeur());
		return d;
	}

	public Integer getIdmessage() {
		return idmessage;
	}

	public void setIdmessage(Integer idmessage) {
		this.idmessage = idmessage;
	}

	public Integer getIdconversation() {
		return idconversation;
	}

	public void setIdconversation(Integer idconversation) {
		this.idconversation = idconversation;
	}

	public Integer getIdAuteur() {
		return idAuteur;
	}

	public void setIdAuteur(Integer idAuteur) {
		this.idAuteur = idAuteur;
	}

	public String getAuteurNom() {
		return auteurNom;
	}

	public void setAuteurNom(String auteurNom) {
		this.auteurNom = auteurNom;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public LocalDateTime getDateenvoi() {
		return dateenvoi;
	}

	public void setDateenvoi(LocalDateTime dateenvoi) {
		this.dateenvoi = dateenvoi;
	}

	public Integer getPrixPropose() {
		return prixPropose;
	}

	public void setPrixPropose(Integer prixPropose) {
		this.prixPropose = prixPropose;
	}

	public Integer getQuantiteProposee() {
		return quantiteProposee;
	}

	public void setQuantiteProposee(Integer quantiteProposee) {
		this.quantiteProposee = quantiteProposee;
	}

	public String getStatutOffre() {
		return statutOffre;
	}

	public void setStatutOffre(String statutOffre) {
		this.statutOffre = statutOffre;
	}

	public Boolean getOffreFinaleVendeur() {
		return offreFinaleVendeur;
	}

	public void setOffreFinaleVendeur(Boolean offreFinaleVendeur) {
		this.offreFinaleVendeur = offreFinaleVendeur;
	}
}
