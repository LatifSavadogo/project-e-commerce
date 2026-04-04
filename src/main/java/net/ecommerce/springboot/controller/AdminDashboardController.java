package net.ecommerce.springboot.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ComplaintRepository;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.service.LivraisonService;
import net.ecommerce.springboot.service.session.ActiveSessionCounter;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminDashboardController {

	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final ComplaintRepository complaintRepository;
	private final EcomTransactionRepository ecomTransactionRepository;
	private final LivraisonService livraisonService;
	private final ActiveSessionCounter activeSessionCounter;

	public AdminDashboardController(UserRepository userRepository, ArticleRepository articleRepository,
			ComplaintRepository complaintRepository, EcomTransactionRepository ecomTransactionRepository,
			LivraisonService livraisonService, ActiveSessionCounter activeSessionCounter) {
		this.userRepository = userRepository;
		this.articleRepository = articleRepository;
		this.complaintRepository = complaintRepository;
		this.ecomTransactionRepository = ecomTransactionRepository;
		this.livraisonService = livraisonService;
		this.activeSessionCounter = activeSessionCounter;
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("usersTotal", userRepository.count());
		m.put("articlesTotal", articleRepository.count());
		m.put("complaintsUnread", complaintRepository.countByLuIsFalse());
		m.put("paymentsTotal", ecomTransactionRepository.count());
		m.put("sessionsActives", activeSessionCounter.getActiveCount());
		m.putAll(livraisonService.adminStats());
		return ResponseEntity.ok(m);
	}
}
