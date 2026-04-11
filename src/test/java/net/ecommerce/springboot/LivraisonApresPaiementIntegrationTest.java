package net.ecommerce.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.ClientLivraisonQrDTO;
import net.ecommerce.springboot.dto.LivraisonLivreurDTO;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.PaymentMethod;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.RoleRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.service.LivraisonService;
import net.ecommerce.springboot.service.PaymentService;

@SpringBootTest(classes = ProjectEcommerceApplication.class)
@Transactional
class LivraisonApresPaiementIntegrationTest {

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ArticleRepository articleRepository;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private LivraisonService livraisonService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void apresPaiement_laLivraisonApparaitDansLesOffresDisponiblesPourUnLivreur() {
		Role roleAcheteur = roleRepository.findByLibroleIgnoreCase(RoleNames.ACHETEUR).orElseThrow();
		Role roleVendeur = roleRepository.findByLibroleIgnoreCase(RoleNames.VENDEUR).orElseThrow();

		User vendeur = userUtil("Vendeur", "Test", "vendeur-livr-test@ecom.t", roleVendeur);
		User acheteur = userUtil("Acheteur", "Test", "acheteur-livr-test@ecom.t", roleAcheteur);
		vendeur.setLatitude(12.35);
		vendeur.setLongitude(-1.51);
		acheteur.setLatitude(12.3714);
		acheteur.setLongitude(-1.5197);
		userRepository.save(vendeur);
		userRepository.save(acheteur);

		Article article = new Article();
		article.setLibarticle("Article test livraison");
		article.setPhoto("placeholder.jpg");
		article.setDescarticle("Description");
		article.setPrixunitaire(5000);
		article.setVendeur(vendeur);
		articleRepository.save(article);

		String refUnique = "TEST-LIVR-" + UUID.randomUUID();
		paymentService.enregistrerPaiement(acheteur, article.getIdarticle(), 1, PaymentMethod.ESPECES, refUnique, null);

		User livreurDemo = userRepository.findByEmail("livreur@demo.ecom").orElseThrow();

		List<LivraisonLivreurDTO> dispo = livraisonService.listDisponiblesPourLivreur(livreurDemo);
		assertFalse(dispo.isEmpty(), "Au moins une livraison EN_ATTENTE sans livreur doit être visible après paiement");
		boolean trouvee = dispo.stream()
				.anyMatch(d -> article.getIdarticle().equals(d.getIdArticle()) && "EN_ATTENTE".equals(d.getStatut()));
		assertEquals(true, trouvee, "La livraison de la transaction payée doit figurer dans la liste disponibles");
	}

	@Test
	void livreurTermineAvecScanQrClient() {
		Role roleAcheteur = roleRepository.findByLibroleIgnoreCase(RoleNames.ACHETEUR).orElseThrow();
		Role roleVendeur = roleRepository.findByLibroleIgnoreCase(RoleNames.VENDEUR).orElseThrow();

		User vendeur = userUtil("V2", "Test", "v2-livr@ecom.t", roleVendeur);
		User acheteur = userUtil("A2", "Test", "a2-livr@ecom.t", roleAcheteur);
		vendeur.setLatitude(12.35);
		vendeur.setLongitude(-1.51);
		acheteur.setLatitude(12.3714);
		acheteur.setLongitude(-1.5197);
		userRepository.save(vendeur);
		userRepository.save(acheteur);

		Article article = new Article();
		article.setLibarticle("Art QR");
		article.setPhoto("p.jpg");
		article.setDescarticle("d");
		article.setPrixunitaire(100);
		article.setVendeur(vendeur);
		articleRepository.save(article);

		var t = paymentService.enregistrerPaiement(acheteur, article.getIdarticle(), 1, PaymentMethod.ESPECES,
				"QR-TEST-" + UUID.randomUUID(), null);
		User livreur = userRepository.findByEmail("livreur@demo.ecom").orElseThrow();

		ClientLivraisonQrDTO qr = livraisonService.buildClientQrPackForBuyer(acheteur, t.getIdtransaction());
		assertNotNull(qr.getQrPayload());

		LivraisonLivreurDTO prise = livraisonService.prendreEnCharge(livreur, qr.getIdlivraison(), TypeEnginLivreur.MOTO);
		assertEquals("EN_COURS", prise.getStatut());

		LivraisonLivreurDTO fin = livraisonService.terminerParScanQrClient(livreur, qr.getQrPayload());
		assertEquals(LivraisonStatut.LIVREE.name(), fin.getStatut());
	}

	@Test
	void paiementRefuseSiAcheteurSansCoordonneesLivraison() {
		Role roleAcheteur = roleRepository.findByLibroleIgnoreCase(RoleNames.ACHETEUR).orElseThrow();
		Role roleVendeur = roleRepository.findByLibroleIgnoreCase(RoleNames.VENDEUR).orElseThrow();
		User vendeur = userUtil("V3", "Test", "v3-gps@ecom.t", roleVendeur);
		User acheteur = userUtil("A3", "Test", "a3-sans-gps@ecom.t", roleAcheteur);
		vendeur.setLatitude(12.3);
		vendeur.setLongitude(-1.5);
		userRepository.save(vendeur);
		userRepository.save(acheteur);
		Article article = new Article();
		article.setLibarticle("Art sans GPS acheteur");
		article.setPhoto("p.jpg");
		article.setDescarticle("d");
		article.setPrixunitaire(200);
		article.setVendeur(vendeur);
		articleRepository.save(article);
		assertThrows(IllegalStateException.class,
				() -> paymentService.enregistrerPaiement(acheteur, article.getIdarticle(), 1, PaymentMethod.ESPECES,
						"NO-GPS-" + UUID.randomUUID(), null));
	}

	private User userUtil(String nom, String prenom, String email, Role role) {
		User u = new User();
		u.setNom(nom);
		u.setPrenom(prenom);
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode("TestPwd!123"));
		u.setRole(role);
		u.setUserupdate("test");
		u.setDateupdate(java.time.LocalDateTime.now());
		return u;
	}
}
