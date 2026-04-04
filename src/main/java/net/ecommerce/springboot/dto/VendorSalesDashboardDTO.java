package net.ecommerce.springboot.dto;

import java.util.ArrayList;
import java.util.List;

public class VendorSalesDashboardDTO {

	private long transactionCount;
	private long totalQuantitySold;
	private long revenueTotal;
	private long averageOrderValue;
	private long revenueLast7Days;
	private long ordersLast7Days;
	private long revenueLast30Days;
	private long ordersLast30Days;
	private List<VendorSalesTimePointDTO> revenueByDay = new ArrayList<>();
	private List<VendorPaymentMethodStatDTO> byPaymentMethod = new ArrayList<>();
	private List<VendorTopArticleDTO> topArticles = new ArrayList<>();

	public long getTransactionCount() {
		return transactionCount;
	}

	public void setTransactionCount(long transactionCount) {
		this.transactionCount = transactionCount;
	}

	public long getTotalQuantitySold() {
		return totalQuantitySold;
	}

	public void setTotalQuantitySold(long totalQuantitySold) {
		this.totalQuantitySold = totalQuantitySold;
	}

	public long getRevenueTotal() {
		return revenueTotal;
	}

	public void setRevenueTotal(long revenueTotal) {
		this.revenueTotal = revenueTotal;
	}

	public long getAverageOrderValue() {
		return averageOrderValue;
	}

	public void setAverageOrderValue(long averageOrderValue) {
		this.averageOrderValue = averageOrderValue;
	}

	public long getRevenueLast7Days() {
		return revenueLast7Days;
	}

	public void setRevenueLast7Days(long revenueLast7Days) {
		this.revenueLast7Days = revenueLast7Days;
	}

	public long getOrdersLast7Days() {
		return ordersLast7Days;
	}

	public void setOrdersLast7Days(long ordersLast7Days) {
		this.ordersLast7Days = ordersLast7Days;
	}

	public long getRevenueLast30Days() {
		return revenueLast30Days;
	}

	public void setRevenueLast30Days(long revenueLast30Days) {
		this.revenueLast30Days = revenueLast30Days;
	}

	public long getOrdersLast30Days() {
		return ordersLast30Days;
	}

	public void setOrdersLast30Days(long ordersLast30Days) {
		this.ordersLast30Days = ordersLast30Days;
	}

	public List<VendorSalesTimePointDTO> getRevenueByDay() {
		return revenueByDay;
	}

	public void setRevenueByDay(List<VendorSalesTimePointDTO> revenueByDay) {
		this.revenueByDay = revenueByDay;
	}

	public List<VendorPaymentMethodStatDTO> getByPaymentMethod() {
		return byPaymentMethod;
	}

	public void setByPaymentMethod(List<VendorPaymentMethodStatDTO> byPaymentMethod) {
		this.byPaymentMethod = byPaymentMethod;
	}

	public List<VendorTopArticleDTO> getTopArticles() {
		return topArticles;
	}

	public void setTopArticles(List<VendorTopArticleDTO> topArticles) {
		this.topArticles = topArticles;
	}
}
