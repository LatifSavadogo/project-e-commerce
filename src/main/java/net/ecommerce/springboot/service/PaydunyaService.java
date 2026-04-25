package net.ecommerce.springboot.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.ecommerce.springboot.config.PaydunyaProperties;
import net.ecommerce.springboot.dto.PaydunyaCheckoutResponseDTO;
import net.ecommerce.springboot.dto.PaydunyaCompleteResponseDTO;
import net.ecommerce.springboot.dto.PaydunyaOrderCheckoutDTO;
import net.ecommerce.springboot.dto.PaymentResultDTO;
import net.ecommerce.springboot.dto.VendorCertificationCheckoutDTO;
import net.ecommerce.springboot.dto.VendorCertificationStatusDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.PaydunyaSettledToken;
import net.ecommerce.springboot.model.PaymentMethod;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.model.VendorCertificationPlan;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.PaydunyaSettledTokenRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.util.PaydunyaMasterKeyHash;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class PaydunyaService {

	public static final String EC_TYPE = "ec_type";
	public static final String EC_BUYER = "ec_buyer";
	public static final String EC_ARTICLE = "ec_article";
	public static final String EC_QTY = "ec_qty";
	public static final String EC_NEG = "ec_neg";
	public static final String EC_LAT = "ec_lat";
	public static final String EC_LON = "ec_lon";
	public static final String EC_PLAN = "ec_plan";

	public static final String KIND_ORDER = "ORDER";
	public static final String KIND_CERT = "CERT";

	private final PaydunyaProperties props;
	private final PaydunyaClient paydunyaClient;
	private final ObjectMapper objectMapper;
	private final PaymentService paymentService;
	private final UserRepository userRepository;
	private final EcomTransactionRepository transactionRepository;
	private final PaydunyaSettledTokenRepository settledTokenRepository;

	public PaydunyaService(PaydunyaProperties props, PaydunyaClient paydunyaClient, ObjectMapper objectMapper,
			PaymentService paymentService, UserRepository userRepository,
			EcomTransactionRepository transactionRepository, PaydunyaSettledTokenRepository settledTokenRepository) {
		this.props = props;
		this.paydunyaClient = paydunyaClient;
		this.objectMapper = objectMapper;
		this.paymentService = paymentService;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.settledTokenRepository = settledTokenRepository;
	}

	public void assertConfigured() {
		if (!props.isConfigured()) {
			throw new IllegalStateException(
					"PayDunya n’est pas configuré. Définissez les variables PAYDUNYA_MASTER_KEY, PAYDUNYA_PRIVATE_KEY et PAYDUNYA_TOKEN (voir application.properties).");
		}
	}

	public PaydunyaCheckoutResponseDTO createOrderCheckout(User buyer, PaydunyaOrderCheckoutDTO dto) {
		assertConfigured();
		int total = paymentService.previewMontantTotal(buyer, dto.getIdArticle(), dto.getQuantite(),
				dto.getPrixUnitaireNegocie(), null);
		User loaded = userRepository.findById(buyer.getIduser())
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + buyer.getIduser()));
		Map<String, String> custom = new LinkedHashMap<>();
		custom.put(EC_TYPE, "ORDER");
		custom.put(EC_BUYER, String.valueOf(loaded.getIduser()));
		custom.put(EC_ARTICLE, String.valueOf(dto.getIdArticle()));
		custom.put(EC_QTY, String.valueOf(dto.getQuantite()));
		if (dto.getPrixUnitaireNegocie() != null) {
			custom.put(EC_NEG, String.valueOf(dto.getPrixUnitaireNegocie()));
		}
		if (dto.getLivraisonLatitude() != null) {
			custom.put(EC_LAT, String.valueOf(dto.getLivraisonLatitude()));
		}
		if (dto.getLivraisonLongitude() != null) {
			custom.put(EC_LON, String.valueOf(dto.getLivraisonLongitude()));
		}
		ObjectNode root = buildInvoiceRoot(total,
				"Paiement commande Ecomarket — article " + dto.getIdArticle() + " (x" + dto.getQuantite() + ")",
				loaded, custom);
		return postAndMapCheckout(root);
	}

	public PaydunyaCheckoutResponseDTO createCertificationCheckout(User seller, VendorCertificationCheckoutDTO dto) {
		assertConfigured();
		User loaded = userRepository.findById(seller.getIduser())
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + seller.getIduser()));
		if (loaded.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(loaded.getRole().getLibrole())) {
			throw new IllegalStateException("Seuls les vendeurs peuvent souscrire à la certification.");
		}
		VendorCertificationPlan plan = dto.getPlan();
		int total = plan.getAmountFcfa();
		Map<String, String> custom = new LinkedHashMap<>();
		custom.put(EC_TYPE, "CERT");
		custom.put(EC_BUYER, String.valueOf(loaded.getIduser()));
		custom.put(EC_PLAN, plan.name());
		ObjectNode root = buildInvoiceRoot(total, "Abonnement vendeur certifié (" + plan.name() + ")", loaded, custom);
		return postAndMapCheckout(root);
	}

	private ObjectNode buildInvoiceRoot(int totalFcfa, String description, User customer,
			Map<String, String> customData) {
		ObjectNode invoice = objectMapper.createObjectNode();
		invoice.put("total_amount", totalFcfa);
		invoice.put("description", description);
		ObjectNode cust = objectMapper.createObjectNode();
		String fullName = (customer.getPrenom() != null ? customer.getPrenom() : "").trim() + " "
				+ (customer.getNom() != null ? customer.getNom() : "").trim();
		cust.put("name", fullName.trim().isEmpty() ? "Client" : fullName.trim());
		cust.put("email", customer.getEmail() != null ? customer.getEmail() : "");
		cust.put("phone", phoneForPaydunya(customer));
		invoice.set("customer", cust);
		ObjectNode custom = objectMapper.createObjectNode();
		for (Map.Entry<String, String> e : customData.entrySet()) {
			custom.put(e.getKey(), e.getValue() != null ? e.getValue() : "");
		}
		invoice.set("custom_data", custom);
		ObjectNode store = objectMapper.createObjectNode();
		store.put("name", props.getStoreName());
		ObjectNode actions = objectMapper.createObjectNode();
		String ret = props.getFrontendReturnBaseUrl();
		actions.put("return_url", ret);
		actions.put("cancel_url", ret);
		actions.put("callback_url", props.callbackUrl());
		ObjectNode root = objectMapper.createObjectNode();
		root.set("invoice", invoice);
		root.set("store", store);
		root.set("actions", actions);
		return root;
	}

	private static String phoneForPaydunya(User customer) {
		if (customer.getCnib() != null && customer.getCnib().matches("\\d{6,}")) {
			return customer.getCnib().replaceAll("\\s+", "");
		}
		return "00000000";
	}

	private PaydunyaCheckoutResponseDTO postAndMapCheckout(ObjectNode root) {
		JsonNode res = paydunyaClient.postCheckoutInvoice(props, root);
		if (!"00".equals(res.path("response_code").asText())) {
			throw new IllegalStateException(
					"PayDunya : " + res.path("response_text").asText("Échec création facture"));
		}
		PaydunyaCheckoutResponseDTO out = new PaydunyaCheckoutResponseDTO();
		out.setCheckoutUrl(res.path("response_text").asText(null));
		out.setInvoiceToken(res.path("token").asText(null));
		out.setDescription(res.path("description").asText(null));
		if (out.getCheckoutUrl() == null || out.getCheckoutUrl().isBlank()) {
			throw new IllegalStateException("PayDunya : URL de paiement absente.");
		}
		return out;
	}

	@Transactional
	public PaydunyaCompleteResponseDTO completeInvoice(String invoiceToken, User authenticatedUser) {
		assertConfigured();
		JsonNode confirm = paydunyaClient.getCheckoutInvoiceConfirm(props, invoiceToken);
		return settleFromConfirm(confirm, invoiceToken, authenticatedUser);
	}

	@Transactional
	public PaydunyaCompleteResponseDTO settleFromIpnJson(String rawJson) {
		assertConfigured();
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			if (root.hasNonNull("data")) {
				root = root.get("data");
			}
			String token = root.path("invoice").path("token").asText("");
			if (token.isBlank()) {
				token = root.path("token").asText("");
			}
			return settleFromConfirm(root, token, null);
		} catch (Exception e) {
			throw new IllegalStateException("IPN PayDunya : JSON invalide.", e);
		}
	}

	private PaydunyaCompleteResponseDTO settleFromConfirm(JsonNode root, String invoiceTokenFallback,
			User authenticatedUser) {
		PaydunyaCompleteResponseDTO out = new PaydunyaCompleteResponseDTO();
		if (!"00".equals(root.path("response_code").asText())) {
			out.setOutcome("FAILED");
			out.setMessage(root.path("response_text").asText("Erreur PayDunya"));
			return out;
		}
		String hash = root.path("hash").asText("");
		if (!PaydunyaMasterKeyHash.matches(props.getMasterKey(), hash)) {
			out.setOutcome("FAILED");
			out.setMessage("Signature PayDunya invalide.");
			return out;
		}
		String status = root.path("status").asText("").toLowerCase();
		if (!"completed".equals(status)) {
			out.setOutcome("NOT_COMPLETED");
			out.setMessage("Paiement non finalisé : " + status);
			return out;
		}
		JsonNode invoice = root.path("invoice");
		String token = invoice.path("token").asText("");
		if (token.isBlank()) {
			token = invoiceTokenFallback;
		}
		int paidAmount = readFcfaAmount(invoice);
		JsonNode cd = root.path("custom_data");
		String type = text(cd, EC_TYPE);
		if ("ORDER".equalsIgnoreCase(type)) {
			return settleOrder(token, paidAmount, cd, authenticatedUser, out);
		}
		if ("CERT".equalsIgnoreCase(type)) {
			return settleCert(token, paidAmount, cd, authenticatedUser, out);
		}
		out.setOutcome("FAILED");
		out.setMessage("Type de facture inconnu (custom_data).");
		return out;
	}

	private PaydunyaCompleteResponseDTO settleOrder(String token, int paidAmount, JsonNode cd,
			User authenticatedUser, PaydunyaCompleteResponseDTO out) {
		int buyerId = parseInt(text(cd, EC_BUYER), "ec_buyer");
		if (authenticatedUser != null && authenticatedUser.getIduser() != buyerId) {
			out.setOutcome("FAILED");
			out.setMessage("Cette facture ne correspond pas à votre compte.");
			return out;
		}
		User buyer = userRepository.findById(buyerId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + buyerId));
		int idArticle = parseInt(text(cd, EC_ARTICLE), EC_ARTICLE);
		int qty = parseInt(text(cd, EC_QTY), EC_QTY);
		Integer nego = blank(text(cd, EC_NEG)) ? null : parseInt(text(cd, EC_NEG), EC_NEG);
		Double lat = blank(text(cd, EC_LAT)) ? null : Double.parseDouble(text(cd, EC_LAT));
		Double lon = blank(text(cd, EC_LON)) ? null : Double.parseDouble(text(cd, EC_LON));
		int expected = paymentService.previewMontantTotal(buyer, idArticle, qty, nego, null);
		if (paidAmount != expected) {
			out.setOutcome("FAILED");
			out.setMessage("Montant PayDunya incohérent avec le panier.");
			return out;
		}
		String ref = "PAYDUNYA:" + token;
		String refHash = TransactionReferenceCrypto.hashForUniqueness(ref);
		var existing = transactionRepository.findFirstByRefExterneHash(refHash);
		if (existing.isPresent()) {
			out.setOutcome("ORDER_SETTLED");
			out.setPayment(PaymentResultDTO.fromEntity(existing.get(), false));
			out.setMessage("Commande déjà enregistrée.");
			return out;
		}
		EcomTransaction saved = paymentService.enregistrerPaiement(buyer, idArticle, qty, PaymentMethod.PAYDUNYA, ref,
				nego, null, lat, lon);
		markSettled(token, KIND_ORDER);
		out.setOutcome("ORDER_SETTLED");
		out.setPayment(PaymentResultDTO.fromEntity(saved, false));
		return out;
	}

	private PaydunyaCompleteResponseDTO settleCert(String token, int paidAmount, JsonNode cd, User authenticatedUser,
			PaydunyaCompleteResponseDTO out) {
		if (settledTokenRepository.existsById(token)) {
			out.setOutcome("CERT_APPLIED");
			out.setMessage("Déjà traité.");
			User u = userRepository.findById(parseInt(text(cd, EC_BUYER), EC_BUYER)).orElse(null);
			if (u != null) {
				out.setCertification(buildCertStatus(u));
			}
			return out;
		}
		int sellerId = parseInt(text(cd, EC_BUYER), EC_BUYER);
		if (authenticatedUser != null && !authenticatedUser.getIduser().equals(sellerId)) {
			out.setOutcome("FAILED");
			out.setMessage("Cette facture ne correspond pas à votre compte.");
			return out;
		}
		User seller = userRepository.findById(sellerId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + sellerId));
		if (seller.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(seller.getRole().getLibrole())) {
			out.setOutcome("FAILED");
			out.setMessage("Compte non vendeur.");
			return out;
		}
		VendorCertificationPlan plan;
		try {
			plan = VendorCertificationPlan.valueOf(text(cd, EC_PLAN));
		} catch (Exception e) {
			out.setOutcome("FAILED");
			out.setMessage("Forfait inconnu.");
			return out;
		}
		if (paidAmount != plan.getAmountFcfa()) {
			out.setOutcome("FAILED");
			out.setMessage("Montant PayDunya incohérent avec le forfait.");
			return out;
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime base = seller.getVendeurCertifieJusqua() != null
				&& seller.getVendeurCertifieJusqua().isAfter(now) ? seller.getVendeurCertifieJusqua() : now;
		LocalDateTime end = plan == VendorCertificationPlan.MONTHLY ? base.plusMonths(1) : base.plusYears(1);
		seller.setVendeurCertifieJusqua(end);
		userRepository.save(seller);
		markSettled(token, KIND_CERT);
		out.setOutcome("CERT_APPLIED");
		out.setCertification(buildCertStatus(seller));
		return out;
	}

	private void markSettled(String token, String kind) {
		if (token == null || token.isBlank()) {
			return;
		}
		if (settledTokenRepository.existsById(token)) {
			return;
		}
		PaydunyaSettledToken st = new PaydunyaSettledToken();
		st.setInvoiceToken(token);
		st.setKind(kind);
		settledTokenRepository.save(st);
	}

	private VendorCertificationStatusDTO buildCertStatus(User seller) {
		VendorCertificationStatusDTO d = new VendorCertificationStatusDTO();
		LocalDateTime until = seller.getVendeurCertifieJusqua();
		boolean active = until != null && until.isAfter(LocalDateTime.now());
		d.setActive(active);
		d.setCertifieJusqua(until);
		d.setMonthlyPriceFcfa(VendorCertificationPlan.MONTHLY.getAmountFcfa());
		d.setYearlyPriceFcfa(VendorCertificationPlan.YEARLY.getAmountFcfa());
		return d;
	}

	private static int readFcfaAmount(JsonNode invoice) {
		JsonNode n = invoice.get("total_amount");
		if (n == null || n.isNull()) {
			return 0;
		}
		if (n.isInt() || n.isLong()) {
			return n.intValue();
		}
		String s = n.asText("0").trim().replace(" ", "");
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static String text(JsonNode parent, String field) {
		if (parent == null || !parent.has(field) || parent.get(field).isNull()) {
			return "";
		}
		return parent.get(field).asText("").trim();
	}

	private static boolean blank(String s) {
		return s == null || s.isBlank();
	}

	private static int parseInt(String raw, String label) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("Donnée facture invalide : " + label);
		}
	}
}
