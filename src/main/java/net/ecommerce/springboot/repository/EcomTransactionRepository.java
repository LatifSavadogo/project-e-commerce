package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.EcomTransaction;

public interface EcomTransactionRepository extends JpaRepository<EcomTransaction, Integer> {

	boolean existsByRefExterneHash(String refExterneHash);

	@Query("select t from EcomTransaction t left join fetch t.livraison l left join fetch l.livreur where t.idtransaction = :id")
	Optional<EcomTransaction> findByIdWithLivraisonAndLivreur(@Param("id") Integer id);

	@Query("select t from EcomTransaction t left join fetch t.livraison where t.acheteur.iduser = :iduser order by t.datecreation desc")
	List<EcomTransaction> findByAcheteur_IduserOrderByDatecreationDesc(@Param("iduser") Integer iduser);

	@Query("select t from EcomTransaction t left join fetch t.livraison where t.vendeur.iduser = :iduser order by t.datecreation desc")
	List<EcomTransaction> findByVendeur_IduserOrderByDatecreationDesc(@Param("iduser") Integer iduser);

	@Query("select t from EcomTransaction t left join fetch t.livraison order by t.datecreation desc")
	List<EcomTransaction> findAllByOrderByDatecreationDesc();

	void deleteByAcheteur_Iduser(Integer iduser);

	void deleteByVendeur_Iduser(Integer iduser);
}
