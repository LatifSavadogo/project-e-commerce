package net.ecommerce.springboot.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.PaymentResultDTO;
import net.ecommerce.springboot.dto.VendorPaymentMethodStatDTO;
import net.ecommerce.springboot.dto.VendorSalesDashboardDTO;
import net.ecommerce.springboot.dto.VendorSalesTimePointDTO;
import net.ecommerce.springboot.dto.VendorTopArticleDTO;

import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.PaymentMethod;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ChatMessageRepository;
import net.ecommerce.springboot.repository.EcomTransactionRepository;

@Service
public class PaymentService {

	private final EcomTransactionRepository transactionRepository;
	private final ArticleRepository articleRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final TransactionReferenceCrypto referenceCrypto;
	private final LivraisonService livraisonService;

	public PaymentService(EcomTransactionRepository transactionRepository, ArticleRepository articleRepository,
			ChatMessageRepository chatMessageRepository, TransactionReferenceCrypto referenceCrypto,
			LivraisonService livraisonService) {
		this.transactionRepository = transactionRepository;
		this.articleRepository = articleRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.referenceCrypto = referenceCrypto;
		this.livraisonService = livraisonService;
	}

	@Transactional
	public EcomTransaction enregistrerPaiement(User acheteur, Integer idArticle, int quantite,
			PaymentMethod moyen, String referenceExterne, Integer prixUnitaireNegocie) {
		return enregistrerPaiement(acheteur, idArticle, quantite, moyen, referenceExterne, prixUnitaireNegocie, null);
	}

	/**
	 * @param negotiationMessageId si non null (ligne panier liée à un message), le paiement doit correspondre à ce message d’accord.
	 */
	@Transactional
	public EcomTransaction enregistrerPaiement(User acheteur, Integer idArticle, int quantite,
			PaymentMethod moyen, String referenceExterne, Integer prixUnitaireNegocie, Integer negotiationMessageId) {
		String refNorm = referenceExterne.trim();
		if (refNorm.isEmpty()) {
			throw new IllegalArgumentException("La référence de transaction est obligatoire.");
		}
		String hash = TransactionReferenceCrypto.hashForUniqueness(refNorm);
		if (transactionRepository.existsByRefExterneHash(hash)) {
			throw new IllegalStateException("Cette référence de transaction est déjà utilisée.");
		}
		Article article = articleRepository.findById(idArticle)
				.orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + idArticle));
		if (article.isBlocked()) {
			throw new IllegalStateException("Cet article n'est plus disponible à l'achat.");
		}
		if (article.getVendeur() == null) {
			throw new IllegalStateException("Article sans vendeur associé.");
		}
		if (article.getVendeur().getIduser().equals(acheteur.getIduser())) {
			throw new IllegalStateException("Vous ne pouvez pas payer pour votre propre annonce.");
		}
		int unit = article.getPrixunitaire();
		if (prixUnitaireNegocie != null) {
			if (prixUnitaireNegocie > article.getPrixunitaire()) {
				throw new IllegalStateException("Le prix négocié ne peut pas dépasser le prix catalogue.");
			}
			long ok = chatMessageRepository.countPayableNegotiatedPrice(prixUnitaireNegocie, idArticle,
					acheteur.getIduser(), article.getVendeur().getIduser(), quantite, negotiationMessageId);
			if (ok < 1) {
				throw new IllegalStateException(
						"Aucun accord à ce prix et à cette quantité (offre acceptée ou dernier prix vendeur) pour cet article.");
			}
			unit = prixUnitaireNegocie;
		}
		long totalLong = (long) unit * quantite;
		if (totalLong > Integer.MAX_VALUE) {
			throw new IllegalStateException("Montant total trop élevé.");
		}
		int total = (int) totalLong;
		int frais = fraisPourMoyen(moyen);

		EcomTransaction t = new EcomTransaction();
		t.setAcheteur(acheteur);
		t.setVendeur(article.getVendeur());
		t.setArticle(article);
		t.setQuantite(quantite);
		t.setPrixUnitaireSnapshot(unit);
		t.setMontantTotal(total);
		t.setFraisAffiches(frais);
		t.setMoyenPaiement(moyen);
		t.setRefExterneHash(hash);
		t.setRefExterneCryptee(referenceCrypto.encrypt(refNorm));
		EcomTransaction saved = transactionRepository.save(t);
		livraisonService.createPendingForTransaction(saved);
		return saved;
	}

	private static int fraisPourMoyen(PaymentMethod m) {
		if (m == PaymentMethod.ORANGE_MONEY || m == PaymentMethod.MOOV_MONEY) {
			return 0;
		}
		return 0;
	}

	public EcomTransaction getTransactionOrThrow(Integer id) {
		return transactionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable : " + id));
	}

	public List<PaymentResultDTO> listByBuyer(Integer iduser) {
		return transactionRepository.findByAcheteur_IduserOrderByDatecreationDesc(iduser).stream()
				.map(PaymentResultDTO::fromEntity)
				.toList();
	}

	public List<PaymentResultDTO> listBySeller(Integer iduser) {
		return transactionRepository.findByVendeur_IduserOrderByDatecreationDesc(iduser).stream()
				.map(PaymentResultDTO::fromEntity)
				.toList();
	}

	/**
	 * Agrégats pour le tableau de bord vendeur : KPI, série 90 jours, répartition moyens de paiement, top articles.
	 */
	public VendorSalesDashboardDTO buildVendorSalesDashboard(Integer sellerId) {
		List<EcomTransaction> list = transactionRepository.findByVendeur_IduserOrderByDatecreationDesc(sellerId);
		VendorSalesDashboardDTO out = new VendorSalesDashboardDTO();
		LocalDate today = LocalDate.now();
		LocalDate start90 = today.minusDays(89);
		long n = list.size();
		out.setTransactionCount(n);
		if (n == 0) {
			for (LocalDate d = start90; !d.isAfter(today); d = d.plusDays(1)) {
				VendorSalesTimePointDTO p = new VendorSalesTimePointDTO();
				p.setDate(d.toString());
				p.setRevenue(0L);
				p.setOrderCount(0L);
				out.getRevenueByDay().add(p);
			}
			return out;
		}
		long revenueTotal = 0L;
		long qtyTotal = 0L;
		LocalDate d7 = today.minusDays(6);
		LocalDate d30 = today.minusDays(29);
		long rev7 = 0L;
		long ord7 = 0L;
		long rev30 = 0L;
		long ord30 = 0L;
		Map<LocalDate, long[]> byDay = new HashMap<>();
		Map<PaymentMethod, long[]> byMethod = new HashMap<>();
		Map<Integer, long[]> byArticle = new HashMap<>();
		Map<Integer, String> articleLabel = new HashMap<>();

		for (EcomTransaction t : list) {
			int mt = t.getMontantTotal();
			int q = t.getQuantite();
			revenueTotal += mt;
			qtyTotal += q;
			LocalDate day = t.getDatecreation().toLocalDate();
			byDay.computeIfAbsent(day, k -> new long[2]);
			long[] da = byDay.get(day);
			da[0] += mt;
			da[1] += 1;
			if (!day.isBefore(d7)) {
				rev7 += mt;
				ord7 += 1;
			}
			if (!day.isBefore(d30)) {
				rev30 += mt;
				ord30 += 1;
			}
			PaymentMethod pm = t.getMoyenPaiement();
			byMethod.computeIfAbsent(pm, k -> new long[2]);
			long[] ma = byMethod.get(pm);
			ma[0] += 1;
			ma[1] += mt;
			int aid = t.getArticle().getIdarticle();
			byArticle.computeIfAbsent(aid, k -> new long[2]);
			long[] aa = byArticle.get(aid);
			aa[0] += mt;
			aa[1] += q;
			articleLabel.putIfAbsent(aid, t.getArticle().getLibarticle());
		}
		out.setRevenueTotal(revenueTotal);
		out.setTotalQuantitySold(qtyTotal);
		out.setAverageOrderValue(revenueTotal / n);
		out.setRevenueLast7Days(rev7);
		out.setOrdersLast7Days(ord7);
		out.setRevenueLast30Days(rev30);
		out.setOrdersLast30Days(ord30);

		for (LocalDate d = start90; !d.isAfter(today); d = d.plusDays(1)) {
			VendorSalesTimePointDTO p = new VendorSalesTimePointDTO();
			p.setDate(d.toString());
			long[] agg = byDay.getOrDefault(d, new long[2]);
			p.setRevenue(agg[0]);
			p.setOrderCount(agg[1]);
			out.getRevenueByDay().add(p);
		}

		byMethod.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().name())).forEach(e -> {
			VendorPaymentMethodStatDTO x = new VendorPaymentMethodStatDTO();
			x.setMethod(e.getKey().name());
			x.setTransactionCount(e.getValue()[0]);
			x.setRevenue(e.getValue()[1]);
			out.getByPaymentMethod().add(x);
		});

		byArticle.entrySet().stream()
				.sorted(Comparator.comparingLong((Map.Entry<Integer, long[]> en) -> en.getValue()[0]).reversed())
				.limit(5)
				.forEach(e -> {
					VendorTopArticleDTO x = new VendorTopArticleDTO();
					x.setIdArticle(e.getKey());
					x.setLibelle(articleLabel.getOrDefault(e.getKey(), ""));
					x.setRevenue(e.getValue()[0]);
					x.setQuantitySold(e.getValue()[1]);
					out.getTopArticles().add(x);
				});

		return out;
	}

	/** Liste complète pour le staff (admin / super-admin). */
	public List<PaymentResultDTO> listAllForStaff() {
		return transactionRepository.findAllByOrderByDatecreationDesc().stream()
				.map(PaymentResultDTO::fromEntity)
				.toList();
	}

	public void assertCanViewReceipt(EcomTransaction t, User user) {
		if (user == null) {
			throw new IllegalStateException("Non authentifié.");
		}
		boolean ok = user.getIduser().equals(t.getAcheteur().getIduser())
				|| user.getIduser().equals(t.getVendeur().getIduser())
				|| ArticleService.isStaffImpl(user)
				|| livraisonService.isAssignedLivreurForTransaction(user, t.getIdtransaction());
		if (!ok) {
			throw new IllegalStateException("Accès non autorisé à ce reçu.");
		}
	}

	public String buildReceiptText(EcomTransaction t) {
		String ref = referenceCrypto.decrypt(t.getRefExterneCryptee());
		StringBuilder sb = new StringBuilder();
		sb.append("========== ECOMARKET - RECU DE PAIEMENT ==========\n");
		sb.append("ID transaction interne: ").append(t.getIdtransaction()).append("\n");
		sb.append("Date: ").append(t.getDatecreation()).append("\n");
		sb.append("Article: ").append(t.getArticle().getLibarticle()).append(" (#").append(t.getArticle().getIdarticle())
				.append(")\n");
		sb.append("Quantite: ").append(t.getQuantite()).append("\n");
		sb.append("Prix unitaire (snapshot): ").append(t.getPrixUnitaireSnapshot()).append("\n");
		sb.append("Montant total: ").append(t.getMontantTotal()).append("\n");
		sb.append("Frais (affiches): ").append(t.getFraisAffiches()).append("\n");
		sb.append("Moyen de paiement: ").append(t.getMoyenPaiement()).append("\n");
		sb.append("Reference externe: ").append(ref).append("\n");
		sb.append("Acheteur: ").append(t.getAcheteur().getEmail()).append("\n");
		sb.append("Vendeur: ").append(t.getVendeur().getEmail()).append("\n");
		sb.append("==================================================\n");
		return sb.toString();
	}
}
