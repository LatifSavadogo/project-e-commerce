package net.ecommerce.springboot.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.SellerRatingCreateDTO;
import net.ecommerce.springboot.dto.VendorRatingSummaryDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.SellerRating;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.LivraisonRepository;
import net.ecommerce.springboot.repository.SellerRatingRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class SellerRatingService {

	private final SellerRatingRepository sellerRatingRepository;
	private final EcomTransactionRepository transactionRepository;
	private final LivraisonRepository livraisonRepository;
	private final UserRepository userRepository;

	public SellerRatingService(SellerRatingRepository sellerRatingRepository,
			EcomTransactionRepository transactionRepository, LivraisonRepository livraisonRepository,
			UserRepository userRepository) {
		this.sellerRatingRepository = sellerRatingRepository;
		this.transactionRepository = transactionRepository;
		this.livraisonRepository = livraisonRepository;
		this.userRepository = userRepository;
	}

	public VendorRatingSummaryDTO getVendorSummary(Integer idVendeur) {
		User v = userRepository.findById(idVendeur)
				.orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable : " + idVendeur));
		if (v.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(v.getRole().getLibrole())) {
			throw new ResourceNotFoundException("Vendeur introuvable : " + idVendeur);
		}
		VendorRatingSummaryDTO out = new VendorRatingSummaryDTO();
		out.setIdVendeur(idVendeur);
		out.setMoyenneEtoiles(0.0);
		out.setNombreAvis(0L);
		List<Object[]> rows = sellerRatingRepository.averageStarsAndCountForSellerIds(Set.of(idVendeur));
		if (!rows.isEmpty()) {
			Object[] r = rows.get(0);
			Number avg = (Number) r[1];
			Number cnt = (Number) r[2];
			out.setMoyenneEtoiles(avg != null ? avg.doubleValue() : 0.0);
			out.setNombreAvis(cnt != null ? cnt.longValue() : 0L);
		}
		LocalDateTime until = v.getVendeurCertifieJusqua();
		out.setCertifie(until != null && until.isAfter(LocalDateTime.now()));
		return out;
	}

	/**
	 * Agrégats note moyenne + nombre d’avis pour plusieurs vendeurs (catalogue).
	 */
	public Map<Integer, double[]> averageStarsBySellerIds(Set<Integer> sellerIds) {
		if (sellerIds == null || sellerIds.isEmpty()) {
			return Map.of();
		}
		Map<Integer, double[]> map = new HashMap<>();
		for (Object[] row : sellerRatingRepository.averageStarsAndCountForSellerIds(sellerIds)) {
			Integer id = (Integer) row[0];
			Number avg = (Number) row[1];
			Number cnt = (Number) row[2];
			map.put(id, new double[] { avg != null ? avg.doubleValue() : 0.0, cnt != null ? cnt.doubleValue() : 0.0 });
		}
		return map;
	}

	public Set<Integer> collectSellerIds(Iterable<net.ecommerce.springboot.model.Article> articles) {
		Set<Integer> ids = new HashSet<>();
		for (net.ecommerce.springboot.model.Article a : articles) {
			if (a.getVendeur() != null) {
				ids.add(a.getVendeur().getIduser());
			}
		}
		return ids;
	}

	@Transactional
	public SellerRating createRatingFromBuyer(User buyer, Integer idtransaction, SellerRatingCreateDTO dto) {
		EcomTransaction t = transactionRepository.findById(idtransaction)
				.orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + idtransaction));
		if (!t.getAcheteur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Seul l’acheteur peut noter cette commande.");
		}
		Livraison l = livraisonRepository.findByTransaction_Idtransaction(idtransaction)
				.orElseThrow(() -> new IllegalStateException("Livraison introuvable pour cette commande."));
		if (l.getStatut() != LivraisonStatut.LIVREE) {
			throw new IllegalStateException("Vous ne pouvez noter le vendeur qu’après livraison confirmée.");
		}
		if (sellerRatingRepository.existsByTransaction_Idtransaction(idtransaction)) {
			throw new IllegalStateException("Cette commande a déjà été notée.");
		}
		SellerRating r = new SellerRating();
		r.setAcheteur(buyer);
		r.setVendeur(t.getVendeur());
		r.setTransaction(t);
		r.setStars(dto.getStars());
		r.setCommentaire(dto.getCommentaire() != null && dto.getCommentaire().isBlank() ? null : dto.getCommentaire());
		return sellerRatingRepository.save(r);
	}
}
