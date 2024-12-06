package com.finc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.finc.entity.Boleto;

public interface BoletosRepository extends JpaRepository<Boleto, Long> {

	// Consulta personalizada para filtrar pelo ano da data de vencimento
	@Query("SELECT b FROM Boleto b WHERE YEAR(b.dataVencimento) = :ano")
	List<Boleto> findByDataVencimento_Year(int ano);

}
