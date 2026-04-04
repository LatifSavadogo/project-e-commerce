package net.ecommerce.springboot.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionReferenceCrypto {

	private static final String AES = "AES";
	private static final String AES_GCM = "AES/GCM/NoPadding";
	private static final int GCM_TAG_BITS = 128;
	private static final int IV_LEN = 12;

	private final SecretKey secretKey;
	private final SecureRandom secureRandom = new SecureRandom();

	public TransactionReferenceCrypto(@Value("${app.crypto.aes-secret}") String passphrase) {
		try {
			byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passphrase.getBytes(StandardCharsets.UTF_8));
			this.secretKey = new SecretKeySpec(keyBytes, AES);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public String encrypt(String plainText) {
		if (plainText == null) {
			throw new IllegalArgumentException("Référence vide.");
		}
		try {
			byte[] iv = new byte[IV_LEN];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(AES_GCM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] combined = new byte[iv.length + cipherBytes.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);
			return java.util.Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			throw new IllegalStateException("Chiffrement impossible", e);
		}
	}

	public String decrypt(String storedBase64) {
		try {
			byte[] combined = java.util.Base64.getDecoder().decode(storedBase64);
			byte[] iv = new byte[IV_LEN];
			System.arraycopy(combined, 0, iv, 0, IV_LEN);
			byte[] cipherBytes = new byte[combined.length - IV_LEN];
			System.arraycopy(combined, IV_LEN, cipherBytes, 0, cipherBytes.length);
			Cipher cipher = Cipher.getInstance(AES_GCM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
			byte[] plain = cipher.doFinal(cipherBytes);
			return new String(plain, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("Déchiffrement impossible", e);
		}
	}

	public static String hashForUniqueness(String referenceExterne) {
		String norm = referenceExterne.trim().toUpperCase();
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(norm.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
