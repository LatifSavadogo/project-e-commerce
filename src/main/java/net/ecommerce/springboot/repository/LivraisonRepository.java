package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.TypeEnginLivreur;

public interface LivraisonRepository extends JpaRepository<Livraison, Integer> {

	Optional<Livraison> findByTransaction_Idtransaction(Integer idtransaction);

	List<Livraison> findByStatutAndLivreurIsNullOrderByDatecreationAsc(LivraisonStatut statut);

	List<Livraison> findByLivreur_IduserOrderByDatecreationDesc(Integer idlivreur);

	long countByLivreur_IduserAndStatut(Integer idlivreur, LivraisonStatut statut);

	long countByLivreur_IduserAndStatutAndTypeEnginUtilise(Integer idlivreur, LivraisonStatut statut,
			TypeEnginLivreur engin);

	long countByStatut(LivraisonStatut statut);

	@Modifying
	@Query("delete from Livraison l where l.transaction.acheteur.iduser = :uid or l.transaction.vendeur.iduser = :uid")
	void deleteByTransactionInvolvingUser(@Param("uid") Integer uid);

	@Modifying
	@Query("update Livraison l set l.livreur = null where l.livreur.iduser = :uid")
	void clearLivreurByUser(@Param("uid") Integer uid);
}
