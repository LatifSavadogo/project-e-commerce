package net.ecommerce.springboot.dto;

import jakarta.validation.constraints.NotNull;

import net.ecommerce.springboot.model.VendorCertificationPlan;

public class VendorCertificationCheckoutDTO {

	@NotNull
	private VendorCertificationPlan plan;

	public VendorCertificationPlan getPlan() {
		return plan;
	}

	public void setPlan(VendorCertificationPlan plan) {
		this.plan = plan;
	}
}
