package net.ecommerce.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

import jakarta.mail.internet.MimeMessage;
import net.ecommerce.springboot.service.PasswordResetService;
import net.ecommerce.springboot.support.GreenMailApplicationContextInitializer;

@SpringBootTest(classes = ProjectEcommerceApplication.class)
@ContextConfiguration(initializers = GreenMailApplicationContextInitializer.class)
@Transactional
class PasswordResetMailIntegrationTest {

	@Autowired
	private PasswordResetService passwordResetService;

	@BeforeEach
	void clearInbox() {
		GreenMail gm = GreenMailApplicationContextInitializer.greenMail();
		if (gm != null) {
			gm.reset();
		}
	}

	@Test
	void demandeReinitialisation_envoieUnEmailMimeUtf8() throws Exception {
		passwordResetService.requestPasswordReset("latif@admin.com");

		GreenMail gm = GreenMailApplicationContextInitializer.greenMail();
		assertTrue(gm.waitForIncomingEmail(8000, 1), "aucun message reçu sur le SMTP de test");

		MimeMessage[] received = gm.getReceivedMessages();
		assertEquals(1, received.length);
		assertEquals("Ecomarket — Réinitialisation du mot de passe", received[0].getSubject());
		String body = GreenMailUtil.getBody(received[0]);
		assertTrue(body.contains("Réinitialiser mon mot de passe") || body.contains("reset-password"),
				"corps attendu avec lien ou bouton de réinitialisation");
	}

	@Test
	void demandeReinitialisation_emailInconnu_nenvoiePasDeMail() throws Exception {
		passwordResetService.requestPasswordReset("inexistant-pour-ce-test@example.invalid");

		GreenMail gm = GreenMailApplicationContextInitializer.greenMail();
		assertFalse(gm.waitForIncomingEmail(2500, 1), "aucun e-mail ne doit partir pour un compte inconnu");
		assertEquals(0, gm.getReceivedMessages().length);
	}
}
