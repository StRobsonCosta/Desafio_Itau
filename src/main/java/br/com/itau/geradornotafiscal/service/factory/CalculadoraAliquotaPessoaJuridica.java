package br.com.itau.geradornotafiscal.service.factory;

import java.util.List;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;

public class CalculadoraAliquotaPessoaJuridica implements CalculadoraAliquota {

	private final Double valorTotalItens;
	private final RegimeTributacaoPJ regimeTributacao;

	public CalculadoraAliquotaPessoaJuridica(RegimeTributacaoPJ regimeTributacao, double valorTotalItens) {
		this.valorTotalItens = valorTotalItens;
		this.regimeTributacao = regimeTributacao;
	}

	@Override
	public List<ItemNotaFiscal> calcularAliquota(List<Item> items, Double aliquotaPercentual) {

		switch (regimeTributacao) {
		case SIMPLES_NACIONAL:
			return calcularAliquotaSimplesNacional(items);
		case LUCRO_REAL:
			return calcularAliquotaLucroReal(items);
		case LUCRO_PRESUMIDO:
			return calcularAliquotaLucroPresumido(items);
		default:
			throw new IllegalArgumentException("Regime de tributação não suportado: " + regimeTributacao);
		}

	}
	
	private List<ItemNotaFiscal> calcularAliquotaSimplesNacional(List<Item> items) {
	    Double aliquotaCalculada;
	    
	    if (valorTotalItens < 1000)
	        aliquotaCalculada = 0.03;
	    else if (valorTotalItens <= 2000)
	        aliquotaCalculada = 0.07;
	    else if (valorTotalItens <= 5000)
	        aliquotaCalculada = 0.13;
	    else
	        aliquotaCalculada = 0.19;

	    return new CalculadoraAliquotaProduto().calcularAliquota(items, aliquotaCalculada);
	}

	private List<ItemNotaFiscal> calcularAliquotaLucroReal(List<Item> items) {
	    Double aliquotaCalculada;
	    
	    if (valorTotalItens < 1000)
	        aliquotaCalculada = 0.03;
	    else if (valorTotalItens <= 2000)
	        aliquotaCalculada = 0.09;
	    else if (valorTotalItens <= 5000)
	        aliquotaCalculada = 0.15;
	    else
	        aliquotaCalculada = 0.20;

	    return new CalculadoraAliquotaProduto().calcularAliquota(items, aliquotaCalculada);
	}

	private List<ItemNotaFiscal> calcularAliquotaLucroPresumido(List<Item> items) {
	    Double aliquotaCalculada;
	    
	    if (valorTotalItens < 1000)
	        aliquotaCalculada = 0.03;
	    else if (valorTotalItens <= 2000)
	        aliquotaCalculada = 0.09;
	    else if (valorTotalItens <= 5000)
	        aliquotaCalculada = 0.16;
	    else
	        aliquotaCalculada = 0.20;

	    return new CalculadoraAliquotaProduto().calcularAliquota(items, aliquotaCalculada);
	}

}
