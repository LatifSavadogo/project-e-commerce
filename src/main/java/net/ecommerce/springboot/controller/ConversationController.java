package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.BuyerFinalOfferDecisionDTO;
import net.ecommerce.springboot.dto.ChatMessageDTO;
import net.ecommerce.springboot.dto.ConversationCreateDTO;
import net.ecommerce.springboot.dto.ConversationDTO;
import net.ecommerce.springboot.dto.MessageCreateDTO;
import net.ecommerce.springboot.dto.OfferRespondDTO;
import net.ecommerce.springboot.dto.SellerFinalPriceDTO;
import net.ecommerce.springboot.model.ChatMessage;
import net.ecommerce.springboot.model.Conversation;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.service.AuthService;
import net.ecommerce.springboot.service.MessagingService;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

	private final MessagingService messagingService;
	private final AuthService authService;

	public ConversationController(MessagingService messagingService, AuthService authService) {
		this.messagingService = messagingService;
		this.authService = authService;
	}

	@PostMapping
	public ResponseEntity<?> open(@Valid @RequestBody ConversationCreateDTO body) {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		try {
			Conversation c = messagingService.openOrGetConversation(u, body.getIdVendeur(), body.getIdArticle());
			return ResponseEntity.ok(ConversationDTO.fromEntity(c));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/mine")
	public ResponseEntity<?> mine() {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(messagingService.listForUser(u.getIduser()).stream().map(ConversationDTO::fromEntity)
				.toList());
	}

	@GetMapping("/{id}/messages")
	public ResponseEntity<?> messages(@PathVariable Integer id) {
		User u = authService.getCurrentUser();
		if (u == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		try {
			Conversation c = messagingService.getConversationOrThrow(id);
			return ResponseEntity.ok(messagingService.listMessages(c, u).stream().map(ChatMessageDTO::fromEntity)
					.toList());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@PostMapping("/{id}/messages")
	public ResponseEntity<?> postMessage(@PathVariable Integer id, @Valid @RequestBody MessageCreateDTO body) {
		User u = requireUser();
		try {
			Conversation c = messagingService.getConversationOrThrow(id);
			ChatMessage m = messagingService.postMessage(c, u, body.getContenu(), body.getPrixPropose(),
					body.getQuantite());
			return ResponseEntity.status(HttpStatus.CREATED).body(ChatMessageDTO.fromEntity(m));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/{id}/messages/{messageId}/offer")
	public ResponseEntity<?> respondToOffer(@PathVariable Integer id, @PathVariable Integer messageId,
			@Valid @RequestBody OfferRespondDTO body) {
		User u = requireUser();
		try {
			ChatMessage m = messagingService.respondToOffer(id, messageId, u, body.getStatut());
			return ResponseEntity.ok(ChatMessageDTO.fromEntity(m));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	/** Acheteur : valider ou refuser le prix final vendeur (tour 3). */
	@PatchMapping("/{id}/messages/{messageId}/final-offer")
	public ResponseEntity<?> buyerRespondToFinalOffer(@PathVariable Integer id,
			@PathVariable Integer messageId, @Valid @RequestBody BuyerFinalOfferDecisionDTO body) {
		User u = requireUser();
		try {
			ChatMessage m = messagingService.buyerRespondToSellerFinal(id, messageId, u,
					Boolean.TRUE.equals(body.getAccept()));
			return ResponseEntity.ok(ChatMessageDTO.fromEntity(m));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	/** Après 2 refus des propositions acheteur : dernier prix fixe (paiement ou renoncement). */
	@PostMapping("/{id}/offre-finale-vendeur")
	public ResponseEntity<?> postOffreFinaleVendeur(@PathVariable Integer id,
			@Valid @RequestBody SellerFinalPriceDTO body) {
		User u = requireUser();
		try {
			Conversation c = messagingService.getConversationOrThrow(id);
			ChatMessage m = messagingService.postSellerFinalOffer(c, u, body.getPrix());
			return ResponseEntity.status(HttpStatus.CREATED).body(ChatMessageDTO.fromEntity(m));
		} catch (IllegalStateException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	private User requireUser() {
		User u = authService.getCurrentUser();
		if (u == null) {
			throw new IllegalStateException("Non authentifié");
		}
		return u;
	}
}
