package br.com.itau.geradornotafiscal.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Finalidade;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.Regiao;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;
import br.com.itau.geradornotafiscal.service.GeradorNotaFiscalService;
import br.com.itau.geradornotafiscal.service.factory.CalculadoraAliquotaFactory;

@Service
public class GeradorNotaFiscalServiceImpl implements GeradorNotaFiscalService{
	
	private final EstoqueService estoqueService;
    private final RegistroService registroService;
    private final EntregaService entregaService;
    private final FinanceiroService financeiroService;
    
	public GeradorNotaFiscalServiceImpl(EstoqueService estoqueService, RegistroService registroService,
			EntregaService entregaService, FinanceiroService financeiroService) {
		this.estoqueService = estoqueService;
		this.registroService = registroService;
		this.entregaService = entregaService;
		this.financeiroService = financeiroService;
	}

	@Override
	public NotaFiscal gerarNotaFiscal(Pedido pedido) {

		Destinatario destinatario = pedido.getDestinatario();
		TipoPessoa tipoPessoa = destinatario.getTipoPessoa();
		RegimeTributacaoPJ regimeTributacao = destinatario.getRegimeTributacao();
		Double valorTotalItens = pedido.getValorTotalItens();

		CalculadoraAliquota calculadoraAliquota = CalculadoraAliquotaFactory.obterCalculadoraAliquota(tipoPessoa, regimeTributacao, valorTotalItens);
		List<ItemNotaFiscal> itemNotaFiscalList = calculadoraAliquota.calcularAliquota(pedido.getItens(), 0.0);

		Double valorFreteComPercentual = calcularFrete(destinatario.getEnderecos(), pedido.getValorFrete());

		NotaFiscal notaFiscal = NotaFiscal.builder()
				.idNotaFiscal(UUID.randomUUID().toString())
				.data(LocalDateTime.now())
				.valorTotalItens(valorTotalItens)
				.valorFrete(valorFreteComPercentual)
				.itens(itemNotaFiscalList)
				.destinatario(pedido.getDestinatario())
				.build();

		enviarParaServicos(notaFiscal);

		return notaFiscal;
	}

	public Double calcularFrete(List<Endereco> enderecos, Double valorFrete) {
		
		Regiao regiao = Optional.ofNullable(enderecos)
			    .orElseThrow(() -> new IllegalArgumentException("A lista de endereços não pode ser nula"))
			    .stream()
	            .filter(endereco -> endereco.getFinalidade() == Finalidade.ENTREGA || endereco.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
	            .map(Endereco::getRegiao)
	            .findFirst()
	            .orElseThrow(() -> new IllegalArgumentException("Região não encontrada"));

		if (regiao == Regiao.NORTE)
			return valorFrete * 1.08;
		else if (regiao == Regiao.NORDESTE)
			return valorFrete * 1.085;
		else if (regiao == Regiao.CENTRO_OESTE)
			return valorFrete * 1.07;
		else if (regiao == Regiao.SUDESTE)
			return valorFrete * 1.048;
		else if (regiao == Regiao.SUL)
			return valorFrete * 1.06;
		
		return valorFrete;
	}

	public void enviarParaServicos(NotaFiscal notaFiscal) {
		estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal);
        registroService.registrarNotaFiscal(notaFiscal);
        entregaService.agendarEntrega(notaFiscal);
        financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal);
	}
}