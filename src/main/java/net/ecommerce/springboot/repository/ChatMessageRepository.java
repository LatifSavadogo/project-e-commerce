package net.ecommerce.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.ChatMessage;
import net.ecommerce.springboot.model.OfferStatus;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

	List<ChatMessage> findByConversation_IdconversationOrderByDateenvoiAsc(Integer idconversation);

	void deleteByConversation_Idconversation(Integer idconversation);

	boolean existsByConversation_IdconversationAndStatutOffre(Integer idconversation, OfferStatus statutOffre);

	List<ChatMessage> findByConversation_IdconversationAndStatutOffre(Integer idconversation,
			OfferStatus statutOffre);

	@Query("SELECT COUNT(m) FROM ChatMessage m JOIN m.conversation c WHERE m.statutOffre = :status "
			+ "AND m.prixPropose = :prix AND c.article.idarticle = :idArticle "
			+ "AND c.acheteur.iduser = :buyer AND c.vendeur.iduser = :seller")
	long countNegotiatedOfferAccepted(@Param("status") OfferStatus status, @Param("prix") Integer prix,
			@Param("idArticle") Integer idArticle, @Param("buyer") Integer buyer, @Param("seller") Integer seller);

	/**
	 * Paiement autorisé : offre acheteur acceptée, ou prix final vendeur validé explicitement par l’acheteur.
	 */
	@Query("SELECT COUNT(m) FROM ChatMessage m JOIN m.conversation c WHERE m.prixPropose = :prix "
			+ "AND c.article.idarticle = :idArticle AND c.acheteur.iduser = :buyerId AND c.vendeur.iduser = :sellerId "
			+ "AND ((m.quantiteProposee IS NULL AND :quantite = 1) OR (m.quantiteProposee IS NOT NULL AND m.quantiteProposee = :quantite)) "
			+ "AND (:negotiationMessageId IS NULL OR m.idmessage = :negotiationMessageId) "
			+ "AND ("
			+ "(m.statutOffre = net.ecommerce.springboot.model.OfferStatus.ACCEPTED "
			+ "AND m.auteur.iduser = :buyerId AND (m.offreFinaleVendeur IS NULL OR m.offreFinaleVendeur = false)) OR "
			+ "(m.statutOffre = net.ecommerce.springboot.model.OfferStatus.VALIDATED "
			+ "AND m.auteur.iduser = :sellerId AND m.offreFinaleVendeur = true) OR "
			+ "(m.statutOffre = net.ecommerce.springboot.model.OfferStatus.ACCEPTED "
			+ "AND m.auteur.iduser = :sellerId AND m.offreFinaleVendeur = true)"
			+ ")")
	long countPayableNegotiatedPrice(@Param("prix") Integer prix, @Param("idArticle") Integer idArticle,
			@Param("buyerId") Integer buyerId, @Param("sellerId") Integer sellerId, @Param("quantite") int quantite,
			@Param("negotiationMessageId") Integer negotiationMessageId);

	int countByConversation_IdconversationAndAuteur_IduserAndPrixProposeIsNotNull(Integer idconversation, Integer iduser);

	boolean existsByConversation_IdconversationAndOffreFinaleVendeurIsTrue(Integer idconversation);
}
