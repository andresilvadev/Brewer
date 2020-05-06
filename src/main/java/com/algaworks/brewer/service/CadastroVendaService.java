package com.algaworks.brewer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.brewer.model.ItemVenda;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Vendas;

@Service
public class CadastroVendaService {

	@Autowired
	private Vendas vendas;
	
	@Transactional
	public void salvar(Venda venda) {
		if(venda.isNova()) {
			venda.setDataCriacao(LocalDateTime.now());
		}
		
		BigDecimal valorTotalItens = venda.getItens().stream()
				.map(ItemVenda::getValorTotal)  		// Mapeia o valor total
				.reduce(BigDecimal::add)				// Soma todo mundo 
				.get();									// Recupera o valor total dos itens
		
		BigDecimal valorTotal = calcularValorTotal(valorTotalItens, venda.getValorFrete(), venda.getValorDesconto());
		
		venda.setValorTotal(valorTotal);				// Seta o valor total
		
		// Cria a data e hora de entrega
		if(venda.getDataEntrega() != null) {
			venda.setDataHoraEntrega(LocalDateTime.of(venda.getDataEntrega(), venda.getHorarioEntrega()));
		}
		
		vendas.save(venda);
	}

	private BigDecimal calcularValorTotal(BigDecimal valorTotalItens, BigDecimal valorFrete, BigDecimal valorDesconto) {
		BigDecimal valorTotal = valorTotalItens			// Valor total recebe o valor total dos itens
				
				// Cria um Big Decimal que pode ser nulo, se for nulo devolve 0 senão devolve o resultado do frete. 
				// (Add) Soma com o valor do frete
				.add(Optional.ofNullable(valorFrete).orElse(BigDecimal.ZERO))			
				.subtract(Optional.ofNullable(valorDesconto).orElse(BigDecimal.ZERO)); 	// Desconta o valor do desconto
		return valorTotal;
	}
}
