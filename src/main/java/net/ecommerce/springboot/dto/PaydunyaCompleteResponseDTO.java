package net.ecommerce.springboot.dto;

public class PaydunyaCompleteResponseDTO {

	/** ORDER_SETTLED | CERT_APPLIED | NOT_COMPLETED | FAILED */
	private String outcome;
	private String message;
	private PaymentResultDTO payment;
	private VendorCertificationStatusDTO certification;

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public PaymentResultDTO getPayment() {
		return payment;
	}

	public void setPayment(PaymentResultDTO payment) {
		this.payment = payment;
	}

	public VendorCertificationStatusDTO getCertification() {
		return certification;
	}

	public void setCertification(VendorCertificationStatusDTO certification) {
		this.certification = certification;
	}
}
