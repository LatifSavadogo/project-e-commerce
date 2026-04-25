package net.ecommerce.springboot.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ecommerce.springboot.dto.VendorCertificationStatusDTO;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.model.VendorCertificationPlan;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.service.AuthService;

@RestController
@RequestMapping("/api/v1/me/vendor-certification")
public class VendorCertificationController {

	private final AuthService authService;
	private final UserRepository userRepository;

	public VendorCertificationController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public ResponseEntity<VendorCertificationStatusDTO> status() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		User fresh = userRepository.findById(u.getIduser())
				.orElseThrow(() -> new IllegalStateException("Utilisateur introuvable."));
		if (fresh.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(fresh.getRole().getLibrole())) {
			throw new IllegalStateException("Réservé aux vendeurs.");
		}
		VendorCertificationStatusDTO d = new VendorCertificationStatusDTO();
		LocalDateTime until = fresh.getVendeurCertifieJusqua();
		d.setCertifieJusqua(until);
		d.setActive(until != null && until.isAfter(LocalDateTime.now()));
		d.setMonthlyPriceFcfa(VendorCertificationPlan.MONTHLY.getAmountFcfa());
		d.setYearlyPriceFcfa(VendorCertificationPlan.YEARLY.getAmountFcfa());
		return ResponseEntity.ok(d);
	}
}
