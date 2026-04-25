package net.ecommerce.springboot.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.ecommerce.springboot.model.SellerRating;

public interface SellerRatingRepository extends JpaRepository<SellerRating, Integer> {

	boolean existsByTransaction_Idtransaction(Integer idtransaction);

	@Query("""
			select s.vendeur.iduser, coalesce(avg(s.stars), 0.0), count(s)
			from SellerRating s
			where s.vendeur.iduser in :ids
			group by s.vendeur.iduser
			""")
	List<Object[]> averageStarsAndCountForSellerIds(@Param("ids") Set<Integer> ids);
}
