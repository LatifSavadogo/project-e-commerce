package net.ecommerce.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.ecommerce.springboot.model.Pays;

public interface PaysRepository extends JpaRepository<Pays, Integer> {

}
