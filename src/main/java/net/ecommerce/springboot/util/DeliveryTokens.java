package net.ecommerce.springboot.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public final class DeliveryTokens {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final char[] DIGITS = "0123456789".toCharArray();

	private DeliveryTokens() {
	}

	/** Jeton secret pour le QR client (non devinable). */
	public static String newClientDeliveryToken() {
		byte[] b = new byte[32];
		RANDOM.nextBytes(b);
		return HexFormat.of().formatHex(b);
	}

	/** Code retrait vendeur : 10 chiffres. */
	public static String newVendorPickupCode() {
		StringBuilder sb = new StringBuilder(10);
		for (int i = 0; i < 10; i++) {
			sb.append(DIGITS[RANDOM.nextInt(DIGITS.length)]);
		}
		return sb.toString();
	}
}
