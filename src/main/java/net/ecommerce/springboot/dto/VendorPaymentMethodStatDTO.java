package net.ecommerce.springboot.dto;

public class VendorPaymentMethodStatDTO {

	private String method;
	private long transactionCount;
	private long revenue;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public long getTransactionCount() {
		return transactionCount;
	}

	public void setTransactionCount(long transactionCount) {
		this.transactionCount = transactionCount;
	}

	public long getRevenue() {
		return revenue;
	}

	public void setRevenue(long revenue) {
		this.revenue = revenue;
	}
}
