package com.finc.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.finc.entity.Boleto;
import com.finc.service.BoletosService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/finc/boletos")
public class BoletosController {

	@Autowired
	private BoletosService boletosService;

	@PostMapping("/cadastro")
	public ResponseEntity<Boleto> create(@RequestBody Boleto boleto) {
		boleto.setSituacao(1);
		Boleto boleto1 = boletosService.save(boleto);

		return ResponseEntity.status(201).body(boleto1);
	}

	@PostMapping("/importar-planilha")
	public ResponseEntity<?> importarPlanilha(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O arquivo está vazio.");
		}

		List<Boleto> boletos = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0); // Considera a primeira aba da planilha.
			for (Row row : sheet) {
				if (row.getRowNum() == 0) {
					// Ignora a primeira linha (cabeçalho).
					continue;
				}

				Boleto boleto = new Boleto();

				// Tipo
				boleto.setTipo(row.getCell(0).getStringCellValue());

				// Descrição
				boleto.setDescricao(row.getCell(1).getStringCellValue());

				// Valor como float
				boleto.setValor((float) row.getCell(2).getNumericCellValue());

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

				// Data de Fechamento como LocalDateTime
				Cell dataCellFechamento = row.getCell(3);
				if (dataCellFechamento.getCellType() == CellType.NUMERIC
						&& DateUtil.isCellDateFormatted(dataCellFechamento)) {
					// Quando a célula contém uma data válida.
					boleto.setDataFechamento(dataCellFechamento.getLocalDateTimeCellValue());
				} else {
					// Quando a célula contém texto ou outro formato.
					String dataStringFechamento = dataCellFechamento.getStringCellValue();
					try {
						LocalDate dataFechamento = LocalDate.parse(dataStringFechamento, formatter);
						boleto.setDataFechamento(dataFechamento.atStartOfDay());
					} catch (DateTimeParseException e) {
						throw new IllegalArgumentException("Data de fechamento inválida: " + dataStringFechamento, e);
					}
				}
				// Data de Vencimento como LocalDateTime
				Cell dataCellVencimento = row.getCell(4);
				if (dataCellVencimento.getCellType() == CellType.NUMERIC
						&& DateUtil.isCellDateFormatted(dataCellVencimento)) {
					// Quando a célula contém uma data válida.
					boleto.setDataVencimento(dataCellVencimento.getLocalDateTimeCellValue());
				} else {
					// Quando a célula contém texto ou outro formato.
					String dataStringVencimento = dataCellVencimento.getStringCellValue();
					try {
						LocalDate dataVencimento = LocalDate.parse(dataStringVencimento, formatter);
						boleto.setDataVencimento(dataVencimento.atStartOfDay());
					} catch (DateTimeParseException e) {
						throw new IllegalArgumentException("Data de vencimento inválida: " + dataStringVencimento, e);
					}
				}

				// Data de Pagamento como LocalDateTime
				Cell dataCellPagamento = row.getCell(5);
				if (dataCellPagamento.getCellType() == CellType.NUMERIC
						&& DateUtil.isCellDateFormatted(dataCellPagamento)) {
					// Quando a célula contém uma data válida.
					boleto.setDataPagamento(dataCellPagamento.getLocalDateTimeCellValue());
				} else {
					// Quando a célula contém texto ou outro formato.
					String dataStringPagamento = dataCellPagamento.getStringCellValue();
					try {
						LocalDate dataPagamento = LocalDate.parse(dataStringPagamento, formatter);
						boleto.setDataPagamento(dataPagamento.atStartOfDay());
					} catch (DateTimeParseException e) {
						throw new IllegalArgumentException("Data de pagamento inválida: " + dataStringPagamento, e);
					}
				}

				// Valor Pago
				boleto.setValorPago((float) row.getCell(6).getNumericCellValue());

				// Reservado
				boleto.setReservado(row.getCell(7).getStringCellValue());

				// Observacoes
				boleto.setObservacoes(row.getCell(8).getStringCellValue());

				// Situacao
				if (row.getCell(9).getStringCellValue() == "PENDENTE") {
					boleto.setSituacao(1);
				} else if (row.getCell(9).getStringCellValue() == "NAO PAGO") {
					boleto.setSituacao(2);
				} else if (row.getCell(9).getStringCellValue() == "PAGO") {
					boleto.setSituacao(3);
				} else if (row.getCell(9).getStringCellValue() == "PAGO PARCIALMENTE") {
					boleto.setSituacao(4);
				} else if (row.getCell(9).getStringCellValue() == "PAGO COM MULTA") {
					boleto.setSituacao(5);
				} else {
					boleto.setSituacao(1);
				}

				boletos.add(boleto);
			}

			// Salva todos os boletos no banco de dados.
			boletosService.saveAll(boletos);

			return ResponseEntity.status(HttpStatus.CREATED).body("Dados importados com sucesso!");

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao interpretar os dados do arquivo.");
		}
	}

	@GetMapping("/carregaTodos")
	public ResponseEntity<List<Boleto>> getAll() {
		List<Boleto> list = boletosService.findAll();

		return ResponseEntity.ok(list);
	}
	
	@GetMapping("/filtrarPorAno")
	public ResponseEntity<List<Boleto>> filtrarPorAno(@RequestParam("ano") int ano) {
	    // Chama o serviço para filtrar os boletos por ano
	    List<Boleto> boletosFiltrados = boletosService.filtrarPorAno(ano);
	    
	    if (boletosFiltrados.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 caso não encontre boletos
	    }
	    
	    return ResponseEntity.ok(boletosFiltrados);
	}
}
