package br.com.itau.geradornotafiscal.service.factory;

import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;

public class CalculadoraAliquotaFactory {
	
	private CalculadoraAliquotaFactory() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não deve ser instanciada.");
    }
	
    public static CalculadoraAliquota obterCalculadoraAliquota(TipoPessoa tipoPessoa, RegimeTributacaoPJ regimeTributacao, double valorTotalItens) {
        if (tipoPessoa == TipoPessoa.FISICA)
            return new CalculadoraAliquotaPessoaFisica(valorTotalItens);
        else if (tipoPessoa == TipoPessoa.JURIDICA)
            return new CalculadoraAliquotaPessoaJuridica(regimeTributacao, valorTotalItens);
        
        throw new IllegalArgumentException("Tipo de Pessoa Inválido: " + tipoPessoa);
    }
}
