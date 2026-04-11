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
 * Chaîne encodée (Base64 URL) pour le vendeur : métadonnées de commande sans montant ni prix unitaire.
 */
public final class VendorOrderReferenceCodec {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private VendorOrderReferenceCodec() {
	}

	public static String encode(EcomTransaction t, Livraison l, String vendorPickupCode) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("v", 1);
		m.put("idtransaction", t.getIdtransaction());
		m.put("idlivraison", l.getIdlivraison());
		if (t.getArticle() != null) {
			m.put("idArticle", t.getArticle().getIdarticle());
			m.put("article", t.getArticle().getLibarticle());
		}
		m.put("quantite", t.getQuantite());
		m.put("codeRetrait", vendorPickupCode);
		m.put("acheteurVille", t.getAcheteur() != null ? t.getAcheteur().getVille() : null);
		try {
			String json = MAPPER.writeValueAsString(m);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Encodage référence vendeur impossible.", e);
		}
	}
}
