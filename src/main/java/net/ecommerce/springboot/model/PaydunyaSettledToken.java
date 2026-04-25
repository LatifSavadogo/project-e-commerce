package net.ecommerce.springboot.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Jeton de facture PayDunya déjà traité (idempotence IPN + retour client). */
@Entity
@Table(name = "PaydunyaSettledToken")
public class PaydunyaSettledToken {

	@Id
	@Column(name = "invoice_token", nullable = false, length = 120)
	private String invoiceToken;

	@Column(nullable = false, length = 32)
	private String kind;

	@Column(nullable = false)
	private Instant settledAt;

	@PrePersist
	public void prePersist() {
		if (settledAt == null) {
			settledAt = Instant.now();
		}
	}

	public String getInvoiceToken() {
		return invoiceToken;
	}

	public void setInvoiceToken(String invoiceToken) {
		this.invoiceToken = invoiceToken;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Instant getSettledAt() {
		return settledAt;
	}

	public void setSettledAt(Instant settledAt) {
		this.settledAt = settledAt;
	}
}
