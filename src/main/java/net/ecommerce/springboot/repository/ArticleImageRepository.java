package net.ecommerce.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.ArticleImage;

import java.util.List;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, Integer> {

    List<ArticleImage> findByProductIdProductOrderBySortOrderAsc(Integer idArticle);

    long countByArticleIdArticle(Integer idArticle);

    void deleteByArticleIdArticle(Integer idArticle);
}