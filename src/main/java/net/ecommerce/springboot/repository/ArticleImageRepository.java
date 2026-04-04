package net.ecommerce.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.ArticleImage;

import java.util.List;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, Integer> {

	List<ArticleImage> findByArticle_IdarticleOrderBySortOrderAsc(Integer idarticle);

	long countByArticle_Idarticle(Integer idarticle);

	void deleteByArticle_Idarticle(Integer idarticle);
}