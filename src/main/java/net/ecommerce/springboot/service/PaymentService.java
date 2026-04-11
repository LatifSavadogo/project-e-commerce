package net.ecommerce.springboot.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.ClientLivraisonQrDTO;
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
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.util.BuyerOrderReceiptQrCodec;
import net.ecommerce.springboot.util.GeoCoordinates;

@Service
public class PaymentService {

	private final EcomTransactionRepository transactionRepository;
	private final ArticleRepository articleRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final TransactionReferenceCrypto referenceCrypto;
	private final LivraisonService livraisonService;
	private final QrPngService qrPngService;

	public PaymentService(EcomTransactionRepository transactionRepository, ArticleRepository articleRepository,
			ChatMessageRepository chatMessageRepository, UserRepository userRepository,
			TransactionReferenceCrypto referenceCrypto, LivraisonService livraisonService, QrPngService qrPngService) {
		this.transactionRepository = transactionRepository;
		this.articleRepository = articleRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.userRepository = userRepository;
		this.referenceCrypto = referenceCrypto;
		this.livraisonService = livraisonService;
		this.qrPngService = qrPngService;
	}

	@Transactional
	public EcomTransaction enregistrerPaiement(User acheteur, Integer idArticle, int quantite,
			PaymentMethod moyen, String referenceExterne, Integer prixUnitaireNegocie) {
		return enregistrerPaiement(acheteur, idArticle, quantite, moyen, referenceExterne, prixUnitaireNegocie, null,
				null, null);
	}

	/**
	 * @param negotiationMessageId si non null (ligne panier liée à un message), le paiement doit correspondre à ce message d’accord.
	 * @param livraisonLatitude / livraisonLongitude optionnels : point de dépôt pour cette commande (sinon domicile du profil).
	 */
	@Transactional
	public EcomTransaction enregistrerPaiement(User acheteur, Integer idArticle, int quantite,
			PaymentMethod moyen, String referenceExterne, Integer prixUnitaireNegocie, Integer negotiationMessageId,
			Double livraisonLatitude, Double livraisonLongitude) {
		User buyer = userRepository.findById(acheteur.getIduser())
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + acheteur.getIduser()));
		GeoCoordinates.requireLatLng(buyer.getLatitude(), buyer.getLongitude(),
				"Renseignez d’abord les coordonnées GPS de votre domicile (Mon compte) avant de payer.");

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
		if (article.getVendeur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Vous ne pouvez pas payer pour votre propre annonce.");
		}
		int unit = article.getPrixunitaire();
		if (prixUnitaireNegocie != null) {
			if (prixUnitaireNegocie > article.getPrixunitaire()) {
				throw new IllegalStateException("Le prix négocié ne peut pas dépasser le prix catalogue.");
			}
			long ok = chatMessageRepository.countPayableNegotiatedPrice(prixUnitaireNegocie, idArticle,
					buyer.getIduser(), article.getVendeur().getIduser(), quantite, negotiationMessageId);
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
		t.setAcheteur(buyer);
		t.setVendeur(article.getVendeur());
		t.setArticle(article);
		t.setQuantite(quantite);
		t.setPrixUnitaireSnapshot(unit);
		t.setMontantTotal(total);
		t.setFraisAffiches(frais);
		t.setMoyenPaiement(moyen);
		t.setRefExterneHash(hash);
		t.setRefExterneCryptee(referenceCrypto.encrypt(refNorm));
		applyLivraisonPointCommande(t, livraisonLatitude, livraisonLongitude);
		EcomTransaction saved = transactionRepository.save(t);
		livraisonService.createPendingForTransaction(saved);
		return saved;
	}

	private static void applyLivraisonPointCommande(EcomTransaction t, Double lat, Double lon) {
		if (lat == null && lon == null) {
			return;
		}
		if (lat == null || lon == null) {
			throw new IllegalArgumentException(
					"Pour livrer à un autre lieu que le domicile, indiquez la latitude et la longitude (point choisi sur la carte).");
		}
		GeoCoordinates.assertValidRange(lat, lon);
		t.setLivraisonLatitude(lat);
		t.setLivraisonLongitude(lon);
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
				.map(t -> PaymentResultDTO.fromEntity(t, false))
				.toList();
	}

	public List<PaymentResultDTO> listBySeller(Integer iduser) {
		return transactionRepository.findByVendeur_IduserOrderByDatecreationDesc(iduser).stream()
				.map(t -> PaymentResultDTO.fromEntity(t, true))
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
				.map(t -> PaymentResultDTO.fromEntity(t, true))
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

	/**
	 * @param requester utilisateur authentifié : le QR « réception livreur » (jeton secret) n’est inclus que si c’est l’acheteur.
	 */
	/** read-write : peut appeler la même chaîne que l’API QR (création du jeton client si besoin). */
	@Transactional
	public byte[] buildReceiptPdf(EcomTransaction t, User requester) {
		EcomTransaction full = transactionRepository.findByIdWithLivraisonAndLivreur(t.getIdtransaction())
				.orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable : " + t.getIdtransaction()));
		String ref = referenceCrypto.decrypt(full.getRefExterneCryptee());

		byte[] qrLivraisonPng = null;
		String qrLivraisonNote = null;
		boolean buyerView = requester != null && full.getAcheteur() != null
				&& requester.getIduser().equals(full.getAcheteur().getIduser());

		if (!buyerView) {
			qrLivraisonNote = "QR réception (livreur) : réservé à l'acheteur — non inclus sur ce document.";
		} else {
			// Identique à l’écran Mes achats : même service, même taille PNG (280 px) que l’API livraison/qr.
			try {
				ClientLivraisonQrDTO pack = livraisonService.buildClientQrPackForBuyer(requester, full.getIdtransaction());
				qrLivraisonPng = qrPngService.encodeQrAsPngBytes(pack.getQrPayload());
			} catch (IllegalStateException ex) {
				qrLivraisonNote = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage()
						: "QR réception indisponible.";
			} catch (ResourceNotFoundException ex) {
				qrLivraisonNote = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage()
						: "Commande introuvable.";
			}
		}

		byte[] qrAchatPng = qrPngService.encodeQrAsPngBytes(BuyerOrderReceiptQrCodec.encode(full));

		try {
			return PaymentReceiptPdfWriter.build(full, ref, qrLivraisonPng, qrLivraisonNote, qrAchatPng);
		} catch (IOException e) {
			throw new IllegalStateException("Impossible de générer le reçu PDF.", e);
		}
	}
}
