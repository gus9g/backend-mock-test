package com.finc.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finc.entity.Boleto;
import com.finc.repository.BoletosRepository;

@Service
public class BoletosService {

	@Autowired
	private BoletosRepository boletoRepository;

	public Boleto save(Boleto boleto) {
		return boletoRepository.save(boleto);
	}

	public List<Boleto> findAll() {
		return boletoRepository.findAll();
	}

	public List<Boleto> saveAll(List<Boleto> boletos) {
		return boletoRepository.saveAll(boletos);
	}

	public List<Boleto> filtrarPorAno(int ano) {
		// Filtra os boletos cujo campo 'dataVencimento' ou 'dataPagamento' tenha o ano
		// passado
		List<Boleto> boletos = boletoRepository.findAll(); // Obtém todos os boletos do banco de dados

		// Filtra os boletos para retornar apenas aqueles com o ano especificado
		return boletos.stream().filter(boleto -> {
			// Supondo que você queira filtrar com base na data de vencimento
			LocalDate dataVencimento = boleto.getDataVencimento().toLocalDate(); // Ajuste conforme o campo
			return dataVencimento.getYear() == ano;
		}).collect(Collectors.toList());
	}
}
