package net.ecommerce.springboot.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;

/**
 * Contenu encodé (Base64 URL) pour le QR « commande / achat » sur le reçu client : identifiants et libellés,
 * sans montants ni données sensibles vendeur.
 */
public final class BuyerOrderReceiptQrCodec {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private BuyerOrderReceiptQrCodec() {
	}

	public static String encode(EcomTransaction t) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("v", 1);
		m.put("type", "ECOM_BUYER_ORDER");
		m.put("idtransaction", t.getIdtransaction());
		Livraison l = t.getLivraison();
		if (l != null) {
			m.put("idlivraison", l.getIdlivraison());
			m.put("statutLivraison", l.getStatut() != null ? l.getStatut().name() : null);
		}
		if (t.getArticle() != null) {
			m.put("idArticle", t.getArticle().getIdarticle());
			m.put("article", t.getArticle().getLibarticle());
		}
		m.put("quantite", t.getQuantite());
		try {
			String json = MAPPER.writeValueAsString(m);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Encodage QR commande impossible.", e);
		}
	}
}
