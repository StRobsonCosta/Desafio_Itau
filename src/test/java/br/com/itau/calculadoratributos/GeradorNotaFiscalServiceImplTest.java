package br.com.itau.calculadoratributos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Finalidade;
import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.Regiao;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;
import br.com.itau.geradornotafiscal.service.factory.CalculadoraAliquotaFactory;
import br.com.itau.geradornotafiscal.service.impl.EntregaService;
import br.com.itau.geradornotafiscal.service.impl.EstoqueService;
import br.com.itau.geradornotafiscal.service.impl.FinanceiroService;
import br.com.itau.geradornotafiscal.service.impl.GeradorNotaFiscalServiceImpl;
import br.com.itau.geradornotafiscal.service.impl.RegistroService;

class GeradorNotaFiscalServiceImplTest {


	@Mock
	private EstoqueService estoqueService;

	@Mock
	private RegistroService registroService;

	@Mock
	private EntregaService entregaService;

	@Mock
	private FinanceiroService financeiroService;

	@InjectMocks
	private GeradorNotaFiscalServiceImpl geradorNotaFiscalService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void shouldGenerateNotaFiscalForTipoPessoaFisicaWithValorTotalItensLessThan500() {
	    Pedido pedido = new Pedido();
	    pedido.setValorTotalItens(400.0);
	    pedido.setValorFrete(100.0);

	    Destinatario destinatario = new Destinatario();
	    destinatario.setTipoPessoa(TipoPessoa.FISICA);

	    Endereco endereco = new Endereco();
	    endereco.setFinalidade(Finalidade.ENTREGA);
	    endereco.setRegiao(Regiao.SUDESTE);
	    destinatario.setEnderecos(Arrays.asList(endereco));

	    pedido.setDestinatario(destinatario);

	    Item item = new Item();
	    item.setValorUnitario(100.0);
	    item.setQuantidade(4);
	    pedido.setItens(Arrays.asList(item));

	    CalculadoraAliquota calculadoraAliquota = mock(CalculadoraAliquota.class);
	    when(calculadoraAliquota.calcularAliquota(any(), anyDouble()))
	            .thenReturn(listarItemNotaFiscalMock(100.0, 0.0, 4));

	    try (MockedStatic<CalculadoraAliquotaFactory> mockedFactory = mockStatic(CalculadoraAliquotaFactory.class)) {
	        mockedFactory.when(() -> CalculadoraAliquotaFactory.obterCalculadoraAliquota(any(), any(), anyDouble()))
	            .thenReturn(calculadoraAliquota);

	        NotaFiscal notaFiscal = geradorNotaFiscalService.gerarNotaFiscal(pedido);

	        assertEquals(400.0, notaFiscal.getValorTotalItens());
	        assertEquals(1, notaFiscal.getItens().size());
	    }
	}

	@Test
	void shouldGenerateNotaFiscalForTipoPessoaJuridicaWithRegimeTributacaoLucroPresumidoAndValorTotalItensGreaterThan5000() {
	    Pedido pedido = new Pedido();
	    pedido.setValorTotalItens(6000.0);
	    pedido.setValorFrete(100.0);

	    Destinatario destinatario = new Destinatario();
	    destinatario.setTipoPessoa(TipoPessoa.JURIDICA);
	    destinatario.setRegimeTributacao(RegimeTributacaoPJ.LUCRO_PRESUMIDO);

	    Endereco endereco = new Endereco();
	    endereco.setFinalidade(Finalidade.ENTREGA);
	    endereco.setRegiao(Regiao.SUDESTE);
	    destinatario.setEnderecos(Arrays.asList(endereco));

	    pedido.setDestinatario(destinatario);

	    Item item = new Item();
	    item.setValorUnitario(1000.0);
	    item.setQuantidade(6);
	    pedido.setItens(Arrays.asList(item));

	    CalculadoraAliquota calculadoraAliquota = mock(CalculadoraAliquota.class);
	    when(calculadoraAliquota.calcularAliquota(any(), anyDouble()))
	            .thenReturn(listarItemNotaFiscalMock(200.0, 1000.0, 6));

	    try (MockedStatic<CalculadoraAliquotaFactory> mockedFactory = mockStatic(CalculadoraAliquotaFactory.class)) {
	        mockedFactory.when(() -> CalculadoraAliquotaFactory.obterCalculadoraAliquota(any(), any(), anyDouble()))
	            .thenReturn(calculadoraAliquota);

	        NotaFiscal notaFiscal = geradorNotaFiscalService.gerarNotaFiscal(pedido);

	        assertEquals(6000.0, notaFiscal.getValorTotalItens());
	        assertEquals(1, notaFiscal.getItens().size());
	        assertEquals(200.0, notaFiscal.getItens().get(0).getValorTributoItem(), 0.001);
	    }
	}

	
	 @Test
	 void shouldGenerateNotaFiscalForTipoPessoaFisicaWithRegimeTributacaoSimplesNacionalAndFreteCalculatedBasedOnRegion() {
		 
	        Pedido pedido = mock(Pedido.class);
	        Destinatario destinatario = mock(Destinatario.class);

	        when(pedido.getDestinatario()).thenReturn(destinatario);
	        when(destinatario.getTipoPessoa()).thenReturn(TipoPessoa.FISICA);
	        when(destinatario.getRegimeTributacao()).thenReturn(RegimeTributacaoPJ.SIMPLES_NACIONAL);
	        when(pedido.getValorTotalItens()).thenReturn(100.0);
	        when(pedido.getItens()).thenReturn(listarItemMock());
	        when(pedido.getValorFrete()).thenReturn(10.0);
	        when(destinatario.getEnderecos()).thenReturn(listarEnderecoMock(Regiao.SUDESTE));

	        CalculadoraAliquota calculadoraAliquota = mock(CalculadoraAliquota.class);
	        when(calculadoraAliquota.calcularAliquota(any(), anyDouble())).thenReturn(listarItemNotaFiscalMock(6.0, 50.0, 2));

	        try (MockedStatic<CalculadoraAliquotaFactory> mockedFactory = mockStatic(CalculadoraAliquotaFactory.class)) {
	            mockedFactory.when(() -> CalculadoraAliquotaFactory.obterCalculadoraAliquota(any(), any(), anyDouble()))
	                    .thenReturn(calculadoraAliquota);

	            NotaFiscal notaFiscal = geradorNotaFiscalService.gerarNotaFiscal(pedido);

	            assertNotNull(notaFiscal);
	            assertEquals(100.0, notaFiscal.getValorTotalItens());
	            assertEquals(10.0 * 1.048, notaFiscal.getValorFrete(), 0.001);
	            verify(estoqueService).enviarNotaFiscalParaBaixaEstoque(notaFiscal);
	            verify(registroService).registrarNotaFiscal(notaFiscal);
	            verify(entregaService).agendarEntrega(notaFiscal);
	            verify(financeiroService).enviarNotaFiscalParaContasReceber(notaFiscal);
	        }
	    }
	 
	 @Test
	 void shouldThrowExceptionWhenNoEntregaOrCobrancaEntregaFinalidadeIsFound() {
	     List<Endereco> enderecos = listarEnderecoMockComFinalidadeDiferente();

	     Double valorFrete = 10.0;

	     Exception exception = assertThrows(IllegalArgumentException.class,
	             () -> geradorNotaFiscalService.calcularFrete(enderecos, valorFrete));

	     assertEquals("Região não encontrada", exception.getMessage());
	 }


	@Test
	void shouldThrowExceptionWhenNoRegiaoIsFoundDuringFreteCalculation() {
		List<Endereco> enderecos = listarEnderecoMockVazio();
		Double valorFrete = 10.0;

		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> geradorNotaFiscalService.calcularFrete(enderecos, valorFrete));
		assertEquals("Região não encontrada", exception.getMessage());
	}
	
	@Test
	void shouldCalculateFreteForRegiaoSudeste() {
		List<Endereco> enderecos = listarEnderecoMock(Regiao.SUDESTE);
		Double valorFrete = 10.0;

		Double valorFreteComPercentual = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

		assertEquals(10.0 * 1.048, valorFreteComPercentual);
	}
	
	@Test
	void shouldCalculateFreteForRegiaoNorte() {
	    List<Endereco> enderecos = listarEnderecoMock(Regiao.NORTE);
	    Double valorFrete = 10.0;

	    Double valorFreteComPercentual = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

	    assertEquals(10.0 * 1.08, valorFreteComPercentual);
	}

	@Test
	void shouldCalculateFreteForRegiaoNordeste() {
	    List<Endereco> enderecos = listarEnderecoMock(Regiao.NORDESTE);
	    Double valorFrete = 10.0;

	    Double valorFreteComPercentual = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

	    assertEquals(10.0 * 1.085, valorFreteComPercentual);
	}

	@Test
	void shouldCalculateFreteForRegiaoCentroOeste() {
	    List<Endereco> enderecos = listarEnderecoMock(Regiao.CENTRO_OESTE);
	    Double valorFrete = 10.0;

	    Double valorFreteComPercentual = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

	    assertEquals(10.0 * 1.07, valorFreteComPercentual);
	}

	@Test
	void shouldCalculateFreteForRegiaoSul() {
	    List<Endereco> enderecos = listarEnderecoMock(Regiao.SUL);
	    Double valorFrete = 10.0;

	    Double valorFreteComPercentual = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

	    assertEquals(10.0 * 1.06, valorFreteComPercentual);
	}

	@Test
	void shouldThrowExceptionWhenEnderecoListIsNullDuringFreteCalculation() {
	    List<Endereco> enderecos = null;
	    Double valorFrete = 10.0;

	    Exception exception = assertThrows(IllegalArgumentException.class,
	            () -> geradorNotaFiscalService.calcularFrete(enderecos, valorFrete));
	    assertEquals("A lista de endereços não pode ser nula", exception.getMessage());
	}
	
	@Test
	void shouldReturnOriginalFreteWhenRegiaoIsNotHandled() {
	    Endereco endereco = new Endereco();
	    endereco.setFinalidade(Finalidade.ENTREGA);
	    endereco.setRegiao(Regiao.DESCONHECIDA);

	    List<Endereco> enderecos = Arrays.asList(endereco);
	    Double valorFrete = 10.0;

	    Double valorFreteFinal = geradorNotaFiscalService.calcularFrete(enderecos, valorFrete);

	    assertEquals(valorFrete, valorFreteFinal);
	}



	private List<Item> listarItemMock() {
		Item itemMock = new Item();
		itemMock.setIdItem("01");
		itemMock.setDescricao("Teclado");
		itemMock.setQuantidade(2);
		itemMock.setValorUnitario(50.0);
		List<Item> itens = Arrays.asList(itemMock);
		return itens;
	}

	private List<Endereco> listarEnderecoMock(Regiao regiao) {
		Endereco enderecoMock = new Endereco();
		enderecoMock.setRegiao(regiao);
		enderecoMock.setFinalidade(Finalidade.ENTREGA);
		enderecoMock.setCep("03105003");

		return Collections.singletonList(enderecoMock);
	}
	
	private List<Endereco> listarEnderecoMockVazio() {
		Endereco enderecoMock = new Endereco();

		return Collections.singletonList(enderecoMock);
	}
	
	private List<Endereco> listarEnderecoMockComFinalidadeDiferente() {
	    Endereco endereco1 = new Endereco();
	    endereco1.setFinalidade(null);
	    endereco1.setRegiao(Regiao.SUDESTE);

	    Endereco endereco2 = new Endereco();
	    endereco2.setFinalidade(Finalidade.OUTROS);
	    endereco2.setRegiao(Regiao.NORDESTE);

	    return Arrays.asList(endereco1, endereco2);
	}


	private List<ItemNotaFiscal> listarItemNotaFiscalMock(Double valorTrib, Double valorUnit, Integer quant) {
		ItemNotaFiscal itemNFMock = new ItemNotaFiscal();
		itemNFMock.setDescricao("Teclado");
		itemNFMock.setQuantidade(quant);
		itemNFMock.setValorTributoItem(valorTrib);
		itemNFMock.setValorUnitario(valorUnit);

		return Collections.singletonList(itemNFMock);
	}

}