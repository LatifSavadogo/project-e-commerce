package net.ecommerce.springboot.dto;

/**
 * Données pour afficher le QR côté client (image + charge utile à scanner par le livreur).
 */
public class ClientLivraisonQrDTO {

	private Integer idtransaction;
	private Integer idlivraison;
	private String articleLibelle;
	private Integer quantite;
	/** Texte exact à encoder dans le QR (ne contient pas les montants). */
	private String qrPayload;
	/** Image PNG en Base64 ; préfixer avec data:image/png;base64, côté front si besoin. */
	private String qrImagePngBase64;
	private String message;

	public Integer getIdtransaction() {
		return idtransaction;
	}

	public void setIdtransaction(Integer idtransaction) {
		this.idtransaction = idtransaction;
	}

	public Integer getIdlivraison() {
		return idlivraison;
	}

	public void setIdlivraison(Integer idlivraison) {
		this.idlivraison = idlivraison;
	}

	public String getArticleLibelle() {
		return articleLibelle;
	}

	public void setArticleLibelle(String articleLibelle) {
		this.articleLibelle = articleLibelle;
	}

	public Integer getQuantite() {
		return quantite;
	}

	public void setQuantite(Integer quantite) {
		this.quantite = quantite;
	}

	public String getQrPayload() {
		return qrPayload;
	}

	public void setQrPayload(String qrPayload) {
		this.qrPayload = qrPayload;
	}

	public String getQrImagePngBase64() {
		return qrImagePngBase64;
	}

	public void setQrImagePngBase64(String qrImagePngBase64) {
		this.qrImagePngBase64 = qrImagePngBase64;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
