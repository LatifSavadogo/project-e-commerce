package net.ecommerce.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

	List<Complaint> findByAuteur_IduserOrderByDatecreationDesc(Integer iduser);

	List<Complaint> findAllByOrderByDatecreationDesc();

	long countByLuIsFalse();

	void deleteByAuteur_Iduser(Integer iduser);

	void deleteByArticle_Vendeur_Iduser(Integer iduserVendeur);
}
