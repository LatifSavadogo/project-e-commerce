package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class MessageCreateDTO {

	@Size(max = 8000)
	private String contenu;

	/** Si renseigné : offre de prix (seul l’acheteur) ; conversation doit avoir un article. */
	@Min(1)
	private Integer prixPropose;

	/** Avec {@link #prixPropose} : quantité commandée visée par la contre-proposition (1–100). */
	@Min(1)
	@Max(100)
	private Integer quantite;

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public Integer getPrixPropose() {
		return prixPropose;
	}

	public void setPrixPropose(Integer prixPropose) {
		this.prixPropose = prixPropose;
	}

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}
}
