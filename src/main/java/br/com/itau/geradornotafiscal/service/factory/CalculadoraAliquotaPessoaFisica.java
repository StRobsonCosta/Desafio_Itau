package br.com.itau.geradornotafiscal.service.factory;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;

import java.util.List;

public class CalculadoraAliquotaPessoaFisica implements CalculadoraAliquota {

    private final double valorTotalItens;

    public CalculadoraAliquotaPessoaFisica(double valorTotalItens) {
        this.valorTotalItens = valorTotalItens;
    }

    @Override
    public List<ItemNotaFiscal> calcularAliquota(List<Item> items, Double aliquotaPercentual) {
        double aliquota;
        if (valorTotalItens < 500)
            aliquota = 0;
        else if (valorTotalItens <= 2000)
            aliquota = 0.12;
        else if (valorTotalItens <= 3500)
            aliquota = 0.15;
        else
            aliquota = 0.17;
        
        return new CalculadoraAliquotaProduto().calcularAliquota(items, aliquota);
    }
}
