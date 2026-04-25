package net.ecommerce.springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ecommerce.springboot.dto.VendorRatingSummaryDTO;
import net.ecommerce.springboot.service.SellerRatingService;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorRatingController {

	private final SellerRatingService sellerRatingService;

	public VendorRatingController(SellerRatingService sellerRatingService) {
		this.sellerRatingService = sellerRatingService;
	}

	@GetMapping("/{id}/rating")
	public ResponseEntity<VendorRatingSummaryDTO> rating(@PathVariable("id") Integer idVendeur) {
		return ResponseEntity.ok(sellerRatingService.getVendorSummary(idVendeur));
	}
}
