package net.ecommerce.springboot.dto;

import java.time.LocalDateTime;

public class VendorCertificationStatusDTO {

	private boolean active;
	private LocalDateTime certifieJusqua;
	private int monthlyPriceFcfa;
	private int yearlyPriceFcfa;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCertifieJusqua() {
		return certifieJusqua;
	}

	public void setCertifieJusqua(LocalDateTime certifieJusqua) {
		this.certifieJusqua = certifieJusqua;
	}

	public int getMonthlyPriceFcfa() {
		return monthlyPriceFcfa;
	}

	public void setMonthlyPriceFcfa(int monthlyPriceFcfa) {
		this.monthlyPriceFcfa = monthlyPriceFcfa;
	}

	public int getYearlyPriceFcfa() {
		return yearlyPriceFcfa;
	}

	public void setYearlyPriceFcfa(int yearlyPriceFcfa) {
		this.yearlyPriceFcfa = yearlyPriceFcfa;
	}
}
