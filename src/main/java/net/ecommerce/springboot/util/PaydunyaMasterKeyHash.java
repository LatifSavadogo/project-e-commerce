package net.ecommerce.springboot.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash SHA-512 de la Master Key (vérification des réponses PayDunya). */
public final class PaydunyaMasterKeyHash {

	private PaydunyaMasterKeyHash() {
	}

	public static String sha512HexLower(String masterKey) {
		if (masterKey == null) {
			throw new IllegalArgumentException("Master key absente.");
		}
		try {
			byte[] digest = MessageDigest.getInstance("SHA-512")
					.digest(masterKey.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean matches(String masterKey, String hashFromPaydunya) {
		if (hashFromPaydunya == null || hashFromPaydunya.isBlank()) {
			return false;
		}
		return sha512HexLower(masterKey).equalsIgnoreCase(hashFromPaydunya.trim());
	}
}
