package net.ecommerce.springboot.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.model.PasswordResetToken;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.PasswordResetTokenRepository;
import net.ecommerce.springboot.repository.UserRepository;

@Service
public class PasswordResetService {

	private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

	private final PasswordResetTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final MailNotificationService mailNotificationService;
	private final SecureRandom secureRandom = new SecureRandom();

	@Value("${app.mail.password-reset.frontend-base-url}")
	private String frontendResetUrl;

	@Value("${app.mail.password-reset.token-validity-minutes:60}")
	private long tokenValidityMinutes;

	public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository,
			UserService userService, MailNotificationService mailNotificationService) {
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.userService = userService;
		this.mailNotificationService = mailNotificationService;
	}

	@Transactional
	public void requestPasswordReset(String email) {
		if (email == null || email.isBlank()) {
			return;
		}
		Optional<User> opt = userRepository.findByEmailIgnoreCase(email.trim());
		if (opt.isEmpty()) {
			return;
		}
		User user = opt.get();
		tokenRepository.deleteUnusedByUserId(user.getIduser());

		String rawToken = generateRawToken();
		String tokenHash = sha256Hex(rawToken);

		PasswordResetToken entity = new PasswordResetToken();
		entity.setUser(user);
		entity.setTokenHash(tokenHash);
		entity.setExpiresAt(LocalDateTime.now().plusMinutes(tokenValidityMinutes));
		tokenRepository.save(entity);

		String link = buildResetLink(rawToken);
		try {
			mailNotificationService.sendPasswordReset(user.getEmail(), link);
		} catch (Exception e) {
			log.error("Impossible d'envoyer l'e-mail de réinitialisation à {}", user.getEmail(), e);
		}
	}

	@Transactional
	public void resetPasswordWithToken(String rawToken, String newPassword) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new IllegalArgumentException("INVALID_TOKEN");
		}
		String hash = sha256Hex(rawToken.trim());
		PasswordResetToken row = tokenRepository.findByTokenHash(hash)
				.orElseThrow(() -> new IllegalArgumentException("INVALID_TOKEN"));
		if (row.getUsedAt() != null || row.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("INVALID_TOKEN");
		}
		userService.resetPassword(row.getUser().getIduser(), newPassword);
		row.setUsedAt(LocalDateTime.now());
		tokenRepository.save(row);
	}

	private String buildResetLink(String rawToken) {
		String base = frontendResetUrl.trim();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
	}

	private String generateRawToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String sha256Hex(String raw) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
