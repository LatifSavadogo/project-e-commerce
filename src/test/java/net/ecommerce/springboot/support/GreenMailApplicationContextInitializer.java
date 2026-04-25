package net.ecommerce.springboot.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

/**
 * Démarre un SMTP de test (port 3025, sans auth) avant le chargement du contexte Spring, aligné sur
 * {@code src/test/resources/application.properties}.
 */
public class GreenMailApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static volatile GreenMail greenMail;

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		synchronized (GreenMailApplicationContextInitializer.class) {
			if (greenMail == null) {
				greenMail = new GreenMail(ServerSetupTest.SMTP);
				greenMail.start();
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						greenMail.stop();
					} catch (Exception ignored) {
						// arrêt JVM
					}
				}));
			}
		}
	}

	public static GreenMail greenMail() {
		return greenMail;
	}
}
