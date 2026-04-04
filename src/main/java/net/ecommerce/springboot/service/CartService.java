package net.ecommerce.springboot.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.CartDTO;
import net.ecommerce.springboot.dto.PaymentResultDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.Cart;
import net.ecommerce.springboot.model.CartItem;
import net.ecommerce.springboot.model.ChatMessage;
import net.ecommerce.springboot.model.Conversation;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.PaymentMethod;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.CartItemRepository;
import net.ecommerce.springboot.repository.CartRepository;
import net.ecommerce.springboot.repository.ChatMessageRepository;

@Service
public class CartService {

	private static final int MAX_QTY = 100;

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ArticleRepository articleRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final PaymentService paymentService;

	public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
			ArticleRepository articleRepository, ChatMessageRepository chatMessageRepository,
			PaymentService paymentService) {
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.articleRepository = articleRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.paymentService = paymentService;
	}

	@Transactional(readOnly = true)
	public CartDTO getCartDto(User buyer) {
		return CartDTO.fromEntity(getOrCreateEmptyCartEntity(buyer));
	}

	private Cart getOrCreateEmptyCartEntity(User buyer) {
		return cartRepository.findByUser_Iduser(buyer.getIduser()).orElseGet(() -> {
			Cart c = new Cart();
			c.setUser(buyer);
			return cartRepository.save(c);
		});
	}

	private void assertArticleBuyableBy(Article article, User buyer) {
		if (article.isBlocked()) {
			throw new IllegalStateException("Cet article n'est plus disponible.");
		}
		if (article.getVendeur() == null) {
			throw new IllegalStateException("Article sans vendeur.");
		}
		if (article.getVendeur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Vous ne pouvez pas ajouter votre propre annonce au panier.");
		}
	}

	@Transactional
	public CartDTO addCatalogueLine(User buyer, Integer idArticle, int quantity) {
		if (quantity < 1 || quantity > MAX_QTY) {
			throw new IllegalArgumentException("Quantité entre 1 et " + MAX_QTY + ".");
		}
		Article article = articleRepository.findById(idArticle)
				.orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + idArticle));
		assertArticleBuyableBy(article, buyer);
		Cart cart = getOrCreateEmptyCartEntity(buyer);
		Optional<CartItem> merge = cart.getItems().stream()
				.filter(ci -> ci.getAgreedMessage() == null && ci.getArticle().getIdarticle().equals(idArticle))
				.findFirst();
		if (merge.isPresent()) {
			CartItem ci = merge.get();
			long sum = (long) ci.getQuantity() + quantity;
			if (sum > MAX_QTY) {
				throw new IllegalStateException("Quantité maximale " + MAX_QTY + " pour cette ligne.");
			}
			ci.setQuantity((int) sum);
			cartRepository.save(cart);
			return CartDTO.fromEntity(cart);
		}
		CartItem line = new CartItem();
		line.setCart(cart);
		line.setArticle(article);
		line.setQuantity(quantity);
		line.setAgreedMessage(null);
		cart.getItems().add(line);
		cartRepository.save(cart);
		return CartDTO.fromEntity(cartRepository.findById(cart.getIdcart()).orElseThrow());
	}

	@Transactional
	public CartDTO addFromNegotiation(User buyer, Integer conversationId, Integer messageId) {
		ChatMessage msg = chatMessageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message introuvable : " + messageId));
		Conversation conv = msg.getConversation();
		if (!conv.getIdconversation().equals(conversationId)) {
			throw new IllegalStateException("Le message n'appartient pas à cette conversation.");
		}
		if (!conv.getAcheteur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Seul l'acheteur peut ajouter cette offre au panier.");
		}
		if (conv.getArticle() == null || msg.getPrixPropose() == null) {
			throw new IllegalStateException("Offre non éligible au panier.");
		}
		int qty = msg.getQuantiteProposee() != null ? msg.getQuantiteProposee() : 1;
		long ok = chatMessageRepository.countPayableNegotiatedPrice(msg.getPrixPropose(),
				conv.getArticle().getIdarticle(), buyer.getIduser(), conv.getVendeur().getIduser(), qty,
				msg.getIdmessage());
		if (ok < 1) {
			throw new IllegalStateException("Cette offre n'est pas payable ou n'est plus valable.");
		}
		if (cartItemRepository.existsByAgreedMessage_Idmessage(messageId)) {
			throw new IllegalStateException("Cette offre est déjà dans le panier.");
		}
		assertArticleBuyableBy(conv.getArticle(), buyer);
		Cart cart = getOrCreateEmptyCartEntity(buyer);
		CartItem line = new CartItem();
		line.setCart(cart);
		line.setArticle(conv.getArticle());
		line.setQuantity(qty);
		line.setAgreedMessage(msg);
		cart.getItems().add(line);
		cartRepository.save(cart);
		return CartDTO.fromEntity(cartRepository.findById(cart.getIdcart()).orElseThrow());
	}

	@Transactional
	public CartDTO updateLineQuantity(User buyer, Integer cartItemId, int quantity) {
		if (quantity < 1 || quantity > MAX_QTY) {
			throw new IllegalArgumentException("Quantité entre 1 et " + MAX_QTY + ".");
		}
		CartItem ci = cartItemRepository.findByIdcartitemAndCart_User_Iduser(cartItemId, buyer.getIduser())
				.orElseThrow(() -> new ResourceNotFoundException("Ligne panier introuvable."));
		if (ci.getAgreedMessage() != null) {
			throw new IllegalStateException(
					"Les lignes issues d'une négociation ont une quantité fixe : retirez la ligne ou finalisez l'achat.");
		}
		ci.setQuantity(quantity);
		cartItemRepository.save(ci);
		return CartDTO.fromEntity(ci.getCart());
	}

	@Transactional
	public CartDTO removeLine(User buyer, Integer cartItemId) {
		CartItem ci = cartItemRepository.findByIdcartitemAndCart_User_Iduser(cartItemId, buyer.getIduser())
				.orElseThrow(() -> new ResourceNotFoundException("Ligne panier introuvable."));
		Cart cart = ci.getCart();
		cart.getItems().remove(ci);
		cartItemRepository.delete(ci);
		cartRepository.save(cart);
		return CartDTO.fromEntity(cartRepository.findById(cart.getIdcart()).orElseThrow());
	}

	@Transactional
	public void clear(User buyer) {
		Cart cart = cartRepository.findByUser_Iduser(buyer.getIduser()).orElse(null);
		if (cart == null) {
			return;
		}
		cart.getItems().clear();
		cartRepository.save(cart);
	}

	@Transactional
	public List<PaymentResultDTO> checkout(User buyer, PaymentMethod method, String referenceBase,
			List<Integer> cartItemIds) {
		if (referenceBase == null || referenceBase.isBlank()) {
			throw new IllegalArgumentException("La référence de transaction est obligatoire.");
		}
		Set<Integer> uniq = new HashSet<>(cartItemIds);
		if (uniq.size() != cartItemIds.size()) {
			throw new IllegalArgumentException("Liste de lignes en double.");
		}
		Cart cart = cartRepository.findByUser_Iduser(buyer.getIduser())
				.orElseThrow(() -> new IllegalStateException("Panier vide."));
		List<PaymentResultDTO> out = new ArrayList<>();
		for (Integer itemId : cartItemIds) {
			CartItem item = cartItemRepository.findByIdcartitemAndCart_User_Iduser(itemId, buyer.getIduser())
					.orElseThrow(() -> new ResourceNotFoundException("Ligne panier introuvable : " + itemId));
			if (!item.getCart().getIdcart().equals(cart.getIdcart())) {
				throw new IllegalStateException("Ligne hors de votre panier.");
			}
			Article article = articleRepository.findById(item.getArticle().getIdarticle())
					.orElseThrow(() -> new ResourceNotFoundException("Article introuvable."));
			assertArticleBuyableBy(article, buyer);
			Integer prixNego = null;
			Integer msgId = null;
			if (item.getAgreedMessage() != null) {
				ChatMessage msg = chatMessageRepository.findById(item.getAgreedMessage().getIdmessage())
						.orElseThrow(() -> new ResourceNotFoundException("Accord introuvable."));
				int agreedQty = msg.getQuantiteProposee() != null ? msg.getQuantiteProposee() : 1;
				if (item.getQuantity() != agreedQty) {
					throw new IllegalStateException("Quantité incohérente avec l'accord négocié.");
				}
				prixNego = msg.getPrixPropose();
				msgId = msg.getIdmessage();
				long stillOk = chatMessageRepository.countPayableNegotiatedPrice(prixNego, article.getIdarticle(),
						buyer.getIduser(), article.getVendeur().getIduser(), agreedQty, msgId);
				if (stillOk < 1) {
					throw new IllegalStateException("L'accord n'est plus valable pour la ligne #" + itemId);
				}
			}
			String uniqueRef = referenceBase.trim() + "|cartItem=" + itemId;
			EcomTransaction t = paymentService.enregistrerPaiement(buyer, article.getIdarticle(), item.getQuantity(),
					method, uniqueRef, prixNego, msgId);
			out.add(PaymentResultDTO.fromEntity(t));
			cart.getItems().remove(item);
			cartItemRepository.delete(item);
		}
		cartRepository.save(cart);
		return out;
	}
}
