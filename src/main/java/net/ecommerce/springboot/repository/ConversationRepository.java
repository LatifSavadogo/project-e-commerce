package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

	Optional<Conversation> findByAcheteur_IduserAndVendeur_IduserAndArticle_Idarticle(Integer idacheteur,
			Integer idvendeur, Integer idarticle);

	Optional<Conversation> findByAcheteur_IduserAndVendeur_IduserAndArticleIsNull(Integer idacheteur,
			Integer idvendeur);

	List<Conversation> findByAcheteur_IduserOrVendeur_IduserOrderByDateupdateDesc(Integer idacheteur,
			Integer idvendeur);
}
