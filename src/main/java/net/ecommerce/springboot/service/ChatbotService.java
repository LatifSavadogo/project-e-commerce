package net.ecommerce.springboot.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import net.ecommerce.springboot.dto.ChatbotReplyDTO;

/**
 * Assistant par règles / regex (sans API externe), aligné cahier Ecomarket.
 */
@Service
public class ChatbotService {

	private static final Pattern PRICE_LIKE = Pattern.compile(
			"(\\d{1,7})\\s*(f\\s*cfa|fcfa|cfa|€|euros?|francs?)?|[\\d.,]+\\s*(f\\s*cfa|fcfa)",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern QTY_LIKE = Pattern.compile(
			"\\b(\\d{1,3})\\s*(pièces?|unités?|articles?|exemplaires?|lots?)\\b", Pattern.CASE_INSENSITIVE);

	public ChatbotReplyDTO reply(String rawMessage) {
		String text = rawMessage == null ? "" : rawMessage.trim();
		ChatbotReplyDTO out = new ChatbotReplyDTO();
		if (text.isEmpty()) {
			out.setReply("Bonjour ! Posez votre question ou choisissez une suggestion ci-dessous.");
			fillSuggestions(out, Set.of());
			return out;
		}

		String lower = text.toLowerCase(Locale.FRENCH);
		Set<String> intents = new LinkedHashSet<>();

		if (matchesGreeting(lower)) {
			intents.add("SALUTATION");
		}
		if (lower.matches(".*\\b(prix|tarif|c[oô]ut|combien|cher|moins cher|réduction|reduction|négocier|negocier)\\b.*")
				|| PRICE_LIKE.matcher(text).find()) {
			intents.add("PRIX");
		}
		if (lower.matches(".*\\b(quantit[ée]|stock|disponible|combien en|nombre d['’]exemplaires)\\b.*")
				|| QTY_LIKE.matcher(text).find()) {
			intents.add("QUANTITE");
		}
		if (lower.matches(".*\\b(urgent|vite|rapidement|asap|au plus vite|immédiat|immediate)\\b.*")) {
			intents.add("URGENCE");
		}
		if (lower.matches(".*\\b(merci|super|parfait|génial|genial|top|ok|d'accord|daccord)\\b.*")) {
			intents.add("SENTIMENT_POSITIF");
		}
		if (lower.matches(".*\\b(cher|trop cher|arnaque|déçu|decu|nul|inacceptable|refus)\\b.*")) {
			intents.add("SENTIMENT_NEGATIF");
		}

		out.setIntentsDetected(new ArrayList<>(intents));
		out.setReply(buildReply(intents));
		out.setTransferSuggested(intents.contains("SENTIMENT_NEGATIF") || intents.contains("URGENCE"));
		fillSuggestions(out, intents);
		return out;
	}

	private static boolean matchesGreeting(String lower) {
		return lower.matches(".*\\b(bonjour|bonsoir|salut|coucou|hello|hi|hey|bonne journ[ée]e)\\b.*")
				|| lower.length() < 22 && lower.matches("^(bonjour|salut|coucou|hello)\\b.*");
	}

	private static String buildReply(Set<String> intents) {
		if (intents.isEmpty()) {
			return "Je peux vous aider sur le prix, la quantité ou la livraison. Souhaitez-vous négocier ou parler au vendeur ?";
		}
		List<String> parts = new ArrayList<>();
		if (intents.contains("SALUTATION")) {
			parts.add("Bonjour et bienvenue sur Ecomarket !");
		}
		if (intents.contains("PRIX")) {
			parts.add("Pour le prix, vous pouvez proposer une offre dans la conversation : le vendeur acceptera ou refusera.");
		}
		if (intents.contains("QUANTITE")) {
			parts.add("Indiquez la quantité souhaitée au moment du paiement (jusqu’à 100 unités par commande).");
		}
		if (intents.contains("URGENCE")) {
			parts.add("Pour une demande urgente, contactez directement le vendeur via la messagerie.");
		}
		if (intents.contains("SENTIMENT_POSITIF")) {
			parts.add("Merci pour votre message ! N’hésitez pas si vous avez d’autres questions.");
		}
		if (intents.contains("SENTIMENT_NEGATIF")) {
			parts.add("Je comprends. Un vendeur humain pourra mieux répondre : je vous invite à lui écrire dans le chat.");
		}
		return String.join(" ", parts);
	}

	private static void fillSuggestions(ChatbotReplyDTO out, Collection<String> intents) {
		List<String> s = new ArrayList<>();
		s.add("Pouvez-vous réduire le prix ?");
		s.add("Quel est votre dernier prix ?");
		s.add("Je prends à ce prix");
		s.add("Le produit est-il encore disponible ?");
		if (intents != null && intents.contains("URGENCE")) {
			s.add(0, "J’ai besoin d’une réponse très vite");
		}
		out.setSuggestions(s);
	}
}
