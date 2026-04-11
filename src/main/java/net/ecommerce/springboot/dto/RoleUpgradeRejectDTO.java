package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.Size;

public class RoleUpgradeRejectDTO {

	@Size(max = 1000)
	private String motif;

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}
}
