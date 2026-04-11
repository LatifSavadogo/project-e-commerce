package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.TypeEnginLivreur;

public interface LivraisonRepository extends JpaRepository<Livraison, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select l from Livraison l where l.idlivraison = :id")
	Optional<Livraison> findByIdForUpdate(@Param("id") Integer id);

	Optional<Livraison> findByTransaction_Idtransaction(Integer idtransaction);

	List<Livraison> findByStatutAndLivreurIsNullOrderByDatecreationAsc(LivraisonStatut statut);

	/**
	 * Offres libres : statut en attente et pas encore de livreur assigné. Fetch explicite pour éviter les erreurs
	 * de chargement lazy hors session et pour des résultats SQL clairs (FK {@code idlivreur} nulle).
	 */
	@Query("""
			select l from Livraison l
			join fetch l.transaction t
			join fetch t.article
			join fetch t.acheteur
			join fetch t.vendeur
			where l.statut = :statut and l.livreur is null
			order by l.datecreation asc
			""")
	List<Livraison> findEnAttenteSansLivreurAvecDetails(@Param("statut") LivraisonStatut statut);

	List<Livraison> findByLivreur_IduserOrderByDatecreationDesc(Integer idlivreur);

	/**
	 * Historique livreur : évite {@code JOIN FETCH} + {@code Pageable} (résultats tronqués / incohérents avec Hibernate).
	 * Le chargement des lignes liées se fait en lazy dans la même transaction {@code @Transactional(readOnly=true)} du service.
	 */
	List<Livraison> findTop200ByLivreur_IduserOrderByDatecreationDesc(Integer idlivreur);

	long countByLivreur_IduserAndStatut(Integer idlivreur, LivraisonStatut statut);

	long countByLivreur_IduserAndStatutAndTypeEnginUtilise(Integer idlivreur, LivraisonStatut statut,
			TypeEnginLivreur engin);

	long countByStatut(LivraisonStatut statut);

	boolean existsByVendorPickupCode(String vendorPickupCode);

	@Modifying
	@Query("delete from Livraison l where l.transaction.acheteur.iduser = :uid or l.transaction.vendeur.iduser = :uid")
	void deleteByTransactionInvolvingUser(@Param("uid") Integer uid);

	@Modifying
	@Query("update Livraison l set l.livreur = null where l.livreur.iduser = :uid")
	void clearLivreurByUser(@Param("uid") Integer uid);
}
