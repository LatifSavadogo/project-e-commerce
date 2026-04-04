package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ecommerce.springboot.dto.PaymentResultDTO;
import net.ecommerce.springboot.service.PaymentService;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminPaymentController {

	private final PaymentService paymentService;

	public AdminPaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping
	public ResponseEntity<List<PaymentResultDTO>> listAll() {
		return ResponseEntity.ok(paymentService.listAllForStaff());
	}
}
