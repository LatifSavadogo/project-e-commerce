package net.ecommerce.springboot.service.session;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Compteur des sessions HTTP actives (création / destruction). Remis à zéro au redémarrage du serveur.
 */
@Component
public class ActiveSessionCounter {

	private final AtomicInteger active = new AtomicInteger(0);

	public void sessionCreated() {
		active.incrementAndGet();
	}

	public void sessionDestroyed() {
		active.updateAndGet(v -> Math.max(0, v - 1));
	}

	public int getActiveCount() {
		return active.get();
	}
}
