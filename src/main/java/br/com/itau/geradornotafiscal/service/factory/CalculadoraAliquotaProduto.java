package br.com.itau.geradornotafiscal.service.factory;

import java.util.List;
import java.util.stream.Collectors;

import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.service.CalculadoraAliquota;

public class CalculadoraAliquotaProduto implements CalculadoraAliquota {

    @Override
    public List<ItemNotaFiscal> calcularAliquota(List<Item> items, Double aliquotaPercentual) {
    	return items.stream()
                .map(item -> ItemNotaFiscal.builder()
                        .idItem(item.getIdItem())
                        .descricao(item.getDescricao())
                        .valorUnitario(item.getValorUnitario())
                        .quantidade(item.getQuantidade())
                        .valorTributoItem(item.getValorUnitario() * aliquotaPercentual)
                        .build())
                .collect(Collectors.toList());
    }
}



