package net.ecommerce.springboot.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.LivreurLivraisonIgnore;

public interface LivreurLivraisonIgnoreRepository extends JpaRepository<LivreurLivraisonIgnore, Integer> {

	@Query("select i.livraison.idlivraison from LivreurLivraisonIgnore i where i.livreur.iduser = :idlivreur")
	Set<Integer> findLivraisonIdsIgnoredByLivreur(@Param("idlivreur") Integer idlivreur);

	boolean existsByLivreur_IduserAndLivraison_Idlivraison(Integer idlivreur, Integer idlivraison);

	@Modifying
	@Query("delete from LivreurLivraisonIgnore i where i.livreur.iduser = :uid")
	void deleteByLivreur_Iduser(@Param("uid") Integer uid);

	@Modifying
	@Query("delete from LivreurLivraisonIgnore i where i.livraison.transaction.acheteur.iduser = :uid or i.livraison.transaction.vendeur.iduser = :uid")
	void deleteForLivraisonsOfUserTransactions(@Param("uid") Integer uid);
}
