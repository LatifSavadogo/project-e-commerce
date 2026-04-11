package net.ecommerce.springboot.dto;

public class SuiviEtapeDTO {

	private String code;
	private String libelle;
	private String date;

	public SuiviEtapeDTO() {
	}

	public SuiviEtapeDTO(String code, String libelle, String date) {
		this.code = code;
		this.libelle = libelle;
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
