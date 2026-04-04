package net.ecommerce.springboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.ChatMessage;
import net.ecommerce.springboot.model.Conversation;
import net.ecommerce.springboot.model.OfferStatus;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ChatMessageRepository;
import net.ecommerce.springboot.repository.ConversationRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class MessagingService {

	private static final int MAX_OFFER_QUANTITE = 100;

	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	/** Réduction minimum (en %) par rapport au prix affiché : plafond = prix × (100 − n) / 100. 0 = désactivé. */
	private final int buyerMinReductionPercentFromList;

	public MessagingService(ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
			UserRepository userRepository, ArticleRepository articleRepository,
			@Value("${app.negotiation.buyer-min-reduction-percent:0}") int buyerMinReductionPercentFromList) {
		this.conversationRepository = conversationRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.userRepository = userRepository;
		this.articleRepository = articleRepository;
		this.buyerMinReductionPercentFromList = Math.max(0, Math.min(99, buyerMinReductionPercentFromList));
	}

	@Transactional
	public Conversation openOrGetConversation(User acheteur, Integer idVendeur, Integer idArticle) {
		if (acheteur.getIduser().equals(idVendeur)) {
			throw new IllegalStateException("Impossible d'ouvrir une conversation avec soi-même.");
		}
		User vendeur = userRepository.findById(idVendeur)
				.orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable : " + idVendeur));
		if (vendeur.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(vendeur.getRole().getLibrole())) {
			throw new IllegalStateException("L'interlocuteur doit être un vendeur.");
		}
		Article resolvedArticle = null;
		if (idArticle != null) {
			resolvedArticle = articleRepository.findById(idArticle)
					.orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + idArticle));
			if (resolvedArticle.isBlocked()) {
				throw new IllegalStateException("Cet article n'est plus disponible.");
			}
			if (resolvedArticle.getVendeur() == null
					|| !resolvedArticle.getVendeur().getIduser().equals(idVendeur)) {
				throw new IllegalStateException("L'article ne correspond pas à ce vendeur.");
			}
		}
		final Article articleForConversation = resolvedArticle;
		Integer aid = acheteur.getIduser();
		if (articleForConversation != null) {
			return conversationRepository
					.findByAcheteur_IduserAndVendeur_IduserAndArticle_Idarticle(aid, idVendeur, idArticle)
					.orElseGet(() -> saveNewConversation(acheteur, vendeur, articleForConversation));
		}
		return conversationRepository.findByAcheteur_IduserAndVendeur_IduserAndArticleIsNull(aid, idVendeur)
				.orElseGet(() -> saveNewConversation(acheteur, vendeur, null));
	}

	private Conversation saveNewConversation(User acheteur, User vendeur, Article article) {
		Conversation c = new Conversation();
		c.setAcheteur(acheteur);
		c.setVendeur(vendeur);
		c.setArticle(article);
		return conversationRepository.save(c);
	}

	public void assertParticipant(Conversation conv, User user) {
		if (user == null) {
			throw new IllegalStateException("Non authentifié.");
		}
		boolean ok = user.getIduser().equals(conv.getAcheteur().getIduser())
				|| user.getIduser().equals(conv.getVendeur().getIduser())
				|| ArticleService.isStaffImpl(user);
		if (!ok) {
			throw new IllegalStateException("Accès non autorisé à cette conversation.");
		}
	}

	public Conversation getConversationOrThrow(Integer id) {
		return conversationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation introuvable : " + id));
	}

	public List<Conversation> listForUser(Integer iduser) {
		return conversationRepository.findByAcheteur_IduserOrVendeur_IduserOrderByDateupdateDesc(iduser, iduser);
	}

	private int countBuyerPrixProposals(Conversation conv) {
		return chatMessageRepository.countByConversation_IdconversationAndAuteur_IduserAndPrixProposeIsNotNull(
				conv.getIdconversation(), conv.getAcheteur().getIduser());
	}

	private List<ChatMessage> orderedMessages(Conversation conv) {
		return chatMessageRepository.findByConversation_IdconversationOrderByDateenvoiAsc(conv.getIdconversation());
	}

	/** La proposition en attente est la 2e proposition de prix de l’acheteur (tour 2). */
	private boolean isSecondBuyerPriceProposal(Conversation conv, ChatMessage pendingBuyerOffer) {
		List<ChatMessage> buyerOffers = orderedMessages(conv).stream()
				.filter(m -> m.getPrixPropose() != null
						&& m.getAuteur().getIduser().equals(conv.getAcheteur().getIduser()))
				.toList();
		if (buyerOffers.size() != 2) {
			return false;
		}
		return buyerOffers.get(1).getIdmessage().equals(pendingBuyerOffer.getIdmessage());
	}

	private boolean buyerCannotSendNewPriceProposal(Conversation conv) {
		Integer aid = conv.getAcheteur().getIduser();
		for (ChatMessage m : orderedMessages(conv)) {
			if (m.getPrixPropose() == null || !m.getAuteur().getIduser().equals(aid)) {
				continue;
			}
			if (m.getStatutOffre() == OfferStatus.ACCEPTED && !Boolean.TRUE.equals(m.getOffreFinaleVendeur())) {
				return true;
			}
		}
		for (ChatMessage m : orderedMessages(conv)) {
			if (!Boolean.TRUE.equals(m.getOffreFinaleVendeur()) || m.getStatutOffre() == null) {
				continue;
			}
			return true;
		}
		return false;
	}

	private int maxAllowedBuyerOffer(int prixCatalogue) {
		if (buyerMinReductionPercentFromList <= 0) {
			return prixCatalogue;
		}
		int cap = (int) Math.floor(prixCatalogue * (100L - buyerMinReductionPercentFromList) / 100.0);
		return Math.max(1, Math.min(cap, prixCatalogue));
	}

	private static int coalesceQuantite(ChatMessage m) {
		return m.getQuantiteProposee() != null ? m.getQuantiteProposee() : 1;
	}

	private int resolveQuantiteForSellerFinal(Conversation conv, List<ChatMessage> pendingList) {
		if (!pendingList.isEmpty()) {
			return coalesceQuantite(pendingList.get(0));
		}
		List<ChatMessage> buyerWithPrice = orderedMessages(conv).stream()
				.filter(m -> m.getPrixPropose() != null
						&& m.getAuteur().getIduser().equals(conv.getAcheteur().getIduser()))
				.toList();
		if (buyerWithPrice.size() < 2) {
			return 1;
		}
		return coalesceQuantite(buyerWithPrice.get(buyerWithPrice.size() - 1));
	}

	/**
	 * Prix final vendeur : soit après deux refus, soit en réponse au tour 2 (refus implicite de la 2e proposition).
	 */
	@Transactional
	public ChatMessage postSellerFinalOffer(Conversation conv, User vendeur, int prix) {
		assertParticipant(conv, vendeur);
		if (!vendeur.getIduser().equals(conv.getVendeur().getIduser())) {
			throw new IllegalStateException("Seul le vendeur peut indiquer le dernier prix.");
		}
		if (conv.getArticle() == null) {
			throw new IllegalStateException("Offre impossible sans article lié à la conversation.");
		}
		if (chatMessageRepository.existsByConversation_IdconversationAndOffreFinaleVendeurIsTrue(
				conv.getIdconversation())) {
			throw new IllegalStateException("Un dernier prix a déjà été fixé pour cette conversation.");
		}
		List<ChatMessage> pendingList = chatMessageRepository.findByConversation_IdconversationAndStatutOffre(
				conv.getIdconversation(), OfferStatus.PENDING);
		if (!pendingList.isEmpty()) {
			if (pendingList.size() > 1) {
				throw new IllegalStateException("Plusieurs offres en attente : situation invalide.");
			}
			ChatMessage pend = pendingList.get(0);
			if (!pend.getAuteur().getIduser().equals(conv.getAcheteur().getIduser())) {
				throw new IllegalStateException("Une offre non acheteur est en attente.");
			}
			if (!isSecondBuyerPriceProposal(conv, pend)) {
				throw new IllegalStateException(
						"Tant que la première proposition est en attente, utilisez accepter ou refuser — pas le prix final.");
			}
			pend.setStatutOffre(OfferStatus.REFUSED);
			chatMessageRepository.save(pend);
		} else {
			List<ChatMessage> buyerWithPrice = orderedMessages(conv).stream()
					.filter(m -> m.getPrixPropose() != null
							&& m.getAuteur().getIduser().equals(conv.getAcheteur().getIduser()))
					.toList();
			if (buyerWithPrice.size() != 2) {
				throw new IllegalStateException(
						"Le dernier prix n'est possible qu'après deux propositions acheteur, ou en réponse à la 2e en attente.");
			}
			if (!buyerWithPrice.stream().allMatch(m -> m.getStatutOffre() == OfferStatus.REFUSED)) {
				throw new IllegalStateException(
						"Les deux propositions acheteur doivent être traitées avant le dernier prix (ou répondez à la 2e en cours).");
			}
		}
		if (prix < 1 || prix > conv.getArticle().getPrixunitaire()) {
			throw new IllegalArgumentException("Le prix doit être entre 1 et le prix affiché de l'article (FCFA).");
		}
		int qtyFinal = resolveQuantiteForSellerFinal(conv, pendingList);
		if (qtyFinal < 1 || qtyFinal > MAX_OFFER_QUANTITE) {
			throw new IllegalStateException("Quantité de référence invalide pour le prix final.");
		}
		String texte = "Prix final proposé par le vendeur : " + prix + " FCFA / unité, quantité : " + qtyFinal
				+ ". En attente de validation par l'acheteur.";
		ChatMessage m = new ChatMessage();
		m.setConversation(conv);
		m.setAuteur(vendeur);
		m.setContenu(texte);
		m.setPrixPropose(prix);
		m.setQuantiteProposee(qtyFinal);
		m.setStatutOffre(OfferStatus.PENDING_BUYER_FINAL);
		m.setOffreFinaleVendeur(Boolean.TRUE);
		conv.setDateupdate(java.time.LocalDateTime.now());
		conversationRepository.save(conv);
		return chatMessageRepository.save(m);
	}

	@Transactional
	public ChatMessage postMessage(Conversation conv, User auteur, String contenu, Integer prixPropose,
			Integer quantiteProposee) {
		assertParticipant(conv, auteur);
		boolean isVendeur = auteur.getIduser().equals(conv.getVendeur().getIduser());
		if (isVendeur && !ArticleService.isStaffImpl(auteur)) {
			throw new IllegalStateException("Le vendeur ne peut pas envoyer de message texte : utilisez les actions de négociation.");
		}
		String text = contenu == null ? "" : contenu.trim();
		if (prixPropose == null && text.isEmpty()) {
			throw new IllegalArgumentException("Le message est vide.");
		}
		if (conv.getArticle() != null && auteur.getIduser().equals(conv.getAcheteur().getIduser())) {
			if (prixPropose == null) {
				throw new IllegalStateException(
						"Les messages texte libres ne sont pas autorisés : envoyez une proposition de prix ou validez le prix final.");
			}
		}
		if (prixPropose != null) {
			if (!auteur.getIduser().equals(conv.getAcheteur().getIduser())) {
				throw new IllegalStateException("Seul l'acheteur peut proposer un prix.");
			}
			if (conv.getArticle() == null) {
				throw new IllegalStateException("Offre impossible sans article lié à la conversation.");
			}
			if (quantiteProposee == null || quantiteProposee < 1 || quantiteProposee > MAX_OFFER_QUANTITE) {
				throw new IllegalArgumentException(
						"Indiquez une quantité entre 1 et " + MAX_OFFER_QUANTITE + " pour votre proposition.");
			}
			if (buyerCannotSendNewPriceProposal(conv)) {
				throw new IllegalStateException("Vous ne pouvez plus envoyer de nouvelle proposition sur cette négociation.");
			}
			if (prixPropose < 1) {
				throw new IllegalArgumentException("Le prix proposé est invalide.");
			}
			int prixCatalogue = conv.getArticle().getPrixunitaire();
			if (prixPropose > prixCatalogue) {
				throw new IllegalStateException("L'offre ne peut pas dépasser le prix affiché de l'article.");
			}
			int maxOffer = maxAllowedBuyerOffer(prixCatalogue);
			if (prixPropose > maxOffer) {
				throw new IllegalStateException("La proposition dépasse le plafond autorisé (prix affiché moins "
						+ buyerMinReductionPercentFromList + "% minimum de réduction).");
			}
			if (countBuyerPrixProposals(conv) >= 2) {
				throw new IllegalStateException(
						"Limite de deux contre-propositions atteinte : le vendeur doit indiquer son dernier prix.");
			}
			if (chatMessageRepository.existsByConversation_IdconversationAndStatutOffre(conv.getIdconversation(),
					OfferStatus.PENDING)) {
				throw new IllegalStateException("Une offre est déjà en attente de réponse du vendeur.");
			}
			if (text.isEmpty()) {
				text = "Proposition : " + prixPropose + " FCFA / unité, quantité : " + quantiteProposee;
			}
		}
		ChatMessage m = new ChatMessage();
		m.setConversation(conv);
		m.setAuteur(auteur);
		m.setContenu(text);
		if (prixPropose != null) {
			m.setPrixPropose(prixPropose);
			m.setQuantiteProposee(quantiteProposee);
			m.setStatutOffre(OfferStatus.PENDING);
			m.setOffreFinaleVendeur(Boolean.FALSE);
		}
		conv.setDateupdate(java.time.LocalDateTime.now());
		conversationRepository.save(conv);
		return chatMessageRepository.save(m);
	}

	@Transactional
	public ChatMessage respondToOffer(Integer conversationId, Integer messageId, User actor, OfferStatus response) {
		if (response != OfferStatus.ACCEPTED && response != OfferStatus.REFUSED) {
			throw new IllegalArgumentException("Réponse attendue : ACCEPTED ou REFUSED.");
		}
		Conversation conv = getConversationOrThrow(conversationId);
		assertParticipant(conv, actor);
		boolean isVendeur = actor.getIduser().equals(conv.getVendeur().getIduser());
		if (!isVendeur && !ArticleService.isStaffImpl(actor)) {
			throw new IllegalStateException("Accès non autorisé : seul le vendeur (ou un administrateur) peut répondre.");
		}
		ChatMessage msg = chatMessageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message introuvable : " + messageId));
		if (!msg.getConversation().getIdconversation().equals(conversationId)) {
			throw new IllegalStateException("Ce message n'appartient pas à cette conversation.");
		}
		if (msg.getPrixPropose() == null || msg.getStatutOffre() != OfferStatus.PENDING) {
			throw new IllegalStateException("Ce message ne contient pas une offre en attente.");
		}
		if (!msg.getAuteur().getIduser().equals(conv.getAcheteur().getIduser())) {
			throw new IllegalStateException("Seules les propositions de l'acheteur peuvent être acceptées ou refusées ici.");
		}
		if (response == OfferStatus.REFUSED) {
			if (isSecondBuyerPriceProposal(conv, msg)) {
				throw new IllegalStateException(
						"Au tour 2, vous ne pouvez pas refuser sans proposer un prix final : utilisez « prix final vendeur ».");
			}
			msg.setStatutOffre(OfferStatus.REFUSED);
			chatMessageRepository.save(msg);
			conv.setDateupdate(java.time.LocalDateTime.now());
			conversationRepository.save(conv);
			return msg;
		}
		List<ChatMessage> pending = chatMessageRepository.findByConversation_IdconversationAndStatutOffre(
				conversationId, OfferStatus.PENDING);
		for (ChatMessage p : pending) {
			if (p.getIdmessage().equals(messageId)) {
				p.setStatutOffre(OfferStatus.ACCEPTED);
			} else {
				p.setStatutOffre(OfferStatus.REFUSED);
			}
			chatMessageRepository.save(p);
		}
		conv.setDateupdate(java.time.LocalDateTime.now());
		conversationRepository.save(conv);
		return chatMessageRepository.findById(messageId).orElseThrow();
	}

	@Transactional
	public ChatMessage buyerRespondToSellerFinal(Integer conversationId, Integer messageId, User buyer, boolean accept) {
		Conversation conv = getConversationOrThrow(conversationId);
		assertParticipant(conv, buyer);
		if (!buyer.getIduser().equals(conv.getAcheteur().getIduser())) {
			throw new IllegalStateException("Seul l'acheteur peut répondre au prix final.");
		}
		ChatMessage msg = chatMessageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message introuvable : " + messageId));
		if (!msg.getConversation().getIdconversation().equals(conversationId)) {
			throw new IllegalStateException("Ce message n'appartient pas à cette conversation.");
		}
		if (!Boolean.TRUE.equals(msg.getOffreFinaleVendeur()) || msg.getStatutOffre() != OfferStatus.PENDING_BUYER_FINAL) {
			throw new IllegalStateException("Ce message n'est pas un prix final en attente de votre validation.");
		}
		msg.setStatutOffre(accept ? OfferStatus.VALIDATED : OfferStatus.EXPIRED);
		conv.setDateupdate(java.time.LocalDateTime.now());
		conversationRepository.save(conv);
		return chatMessageRepository.save(msg);
	}

	public List<ChatMessage> listMessages(Conversation conv, User reader) {
		assertParticipant(conv, reader);
		return chatMessageRepository.findByConversation_IdconversationOrderByDateenvoiAsc(conv.getIdconversation());
	}
}
