package net.ecommerce.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import net.ecommerce.springboot.model.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

	List<Article> findByBlockedIsFalseOrderByDateupdateDesc();

	List<Article> findByBlockedIsFalseAndVendeur_VendeurInternationalIsTrueOrderByDateupdateDesc();

	List<Article> findAllByOrderByDateupdateDesc();

	List<Article> findByVendeur_IduserOrderByDateupdateDesc(Integer idVendeur);

	void deleteByVendeur_Iduser(Integer iduser);
}
