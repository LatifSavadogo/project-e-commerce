package net.ecommerce.springboot.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailNotificationService {

	private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);

	private final JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String fromAddress;

	@Value("${app.mail.from-personal:Ecomarket}")
	private String fromPersonal;

	/**
	 * Si false : aucun envoi SMTP (ex. dev local) — le lien est journalisé en WARN.
	 */
	@Value("${app.mail.enabled:true}")
	private boolean mailEnabled;

	public MailNotificationService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendPasswordReset(String toEmail, String resetLink) {
		if (fromAddress == null || fromAddress.isBlank()) {
			throw new IllegalStateException(
					"app.mail.from n'est pas configuré. Définissez MAIL_FROM ou MAIL_USERNAME dans l'environnement.");
		}
		String textBody = buildResetBodyText(resetLink);
		String htmlBody = buildResetBodyHtml(resetLink);

		if (!mailEnabled) {
			log.warn("[app.mail.enabled=false] E-mail non envoyé. Destinataire={} — Lien de réinitialisation : {}", toEmail,
					resetLink);
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setFrom(fromAddress, fromPersonal);
			helper.setTo(toEmail);
			helper.setSubject("Ecomarket — Réinitialisation du mot de passe");
			helper.setText(textBody, htmlBody);
			mailSender.send(message);
			log.info("E-mail de réinitialisation (MIME UTF-8) envoyé à {}", toEmail);
		} catch (Exception e) {
			log.error("Échec envoi SMTP vers {} (vérifiez MAIL_HOST, MAIL_PORT, MAIL_USERNAME, mot de passe d'application, SSL/TLS).",
					toEmail, e);
			throw new IllegalStateException("Envoi e-mail impossible : " + e.getMessage(), e);
		}
	}

	private static String buildResetBodyText(String resetLink) {
		return """
				Bonjour,

				Pour choisir un nouveau mot de passe sur Ecomarket, ouvrez le lien ci-dessous (il expire après la durée configurée côté serveur) :

				%s

				Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.

				Cordialement,
				L'équipe Ecomarket
				""".formatted(resetLink);
	}

	private static String buildResetBodyHtml(String resetLink) {
		return """
				<!DOCTYPE html>
				<html lang="fr">
				<head><meta charset="UTF-8"></head>
				<body style="font-family:Segoe UI,Roboto,sans-serif;line-height:1.5;color:#1e293b;">
				<p>Bonjour,</p>
				<p>Pour définir un <strong>nouveau mot de passe</strong> sur Ecomarket, utilisez le bouton ci-dessous :</p>
				<p style="margin:24px 0;">
				  <a href="%s" style="background:#17806f;color:#fff;padding:12px 20px;border-radius:8px;text-decoration:none;display:inline-block;">
				    Réinitialiser mon mot de passe
				  </a>
				</p>
				<p style="font-size:0.9em;color:#64748b;">Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br/>
				<a href="%s">%s</a></p>
				<p style="font-size:0.9em;">Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.</p>
				<p>Cordialement,<br/>L'équipe Ecomarket</p>
				</body>
				</html>
				"""
				.formatted(resetLink, resetLink, resetLink);
	}
}
