package br.com.itau.geradornotafiscal.service;

import java.util.List;
import java.util.stream.Collectors;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;

public class CalculadoraAliquotaProduto {
//    private static List<ItemNotaFiscal> itemNotaFiscalList = new ArrayList<>();
//
//    public List<ItemNotaFiscal> calcularAliquota(List<Item> items, double aliquotaPercentual) {
//
//        for (Item item : items) {
//            double valorTributo = item.getValorUnitario() * aliquotaPercentual;
//            ItemNotaFiscal itemNotaFiscal = ItemNotaFiscal.builder()
//                    .idItem(item.getIdItem())
//                    .descricao(item.getDescricao())
//                    .valorUnitario(item.getValorUnitario())
//                    .quantidade(item.getQuantidade())
//                    .valorTributoItem(valorTributo)
//                    .build();
//            itemNotaFiscalList.add(itemNotaFiscal);
//        }
//        return itemNotaFiscalList;
//    }
	
	public List<ItemNotaFiscal> calcularAliquota(Pedido pedido) {
        double aliquota = calcularAliquotaPorTipoPessoa(pedido.getDestinatario(), pedido.getValorTotalItens());
        return calcularItens(pedido.getItens(), aliquota);
    }

    private Double calcularAliquotaPorTipoPessoa(Destinatario destinatario, double valorTotalItens) {
        if (destinatario.getTipoPessoa() == TipoPessoa.FISICA) {
            return calcularAliquotaPessoaFisica(valorTotalItens);
        } else if (destinatario.getTipoPessoa() == TipoPessoa.JURIDICA) {
            return calcularAliquotaPessoaJuridica(destinatario.getRegimeTributacao(), valorTotalItens);
        }
        throw new IllegalArgumentException("Tipo de pessoa não suportado");
    }

    private Double calcularAliquotaPessoaFisica(double valorTotalItens) {
        if (valorTotalItens < 500) {
            return 0.0;
        } else if (valorTotalItens <= 2000) {
            return 0.12;
        } else if (valorTotalItens <= 3500) {
            return 0.15;
        } else {
            return 0.17;
        }
    }

    private Double calcularAliquotaPessoaJuridica(RegimeTributacaoPJ regimeTributacao, double valorTotalItens) {
        if (regimeTributacao == RegimeTributacaoPJ.SIMPLES_NACIONAL) {
            return calcularAliquotaSimplesNacional(valorTotalItens);
        } else if (regimeTributacao == RegimeTributacaoPJ.LUCRO_REAL) {
            return calcularAliquotaLucroReal(valorTotalItens);
        } else if (regimeTributacao == RegimeTributacaoPJ.LUCRO_PRESUMIDO) {
            return calcularAliquotaLucroPresumido(valorTotalItens);
        }
        throw new IllegalArgumentException("Regime de tributação não suportado");
    }

    private Double calcularAliquotaSimplesNacional(double valorTotalItens) {
        if (valorTotalItens < 1000) {
            return 0.03;
        } else if (valorTotalItens <= 2000) {
            return 0.07;
        } else if (valorTotalItens <= 5000) {
            return 0.13;
        } else {
            return 0.19;
        }
    }

    private Double calcularAliquotaLucroReal(double valorTotalItens) {
        if (valorTotalItens < 1000) {
            return 0.03;
        } else if (valorTotalItens <= 2000) {
            return 0.09;
        } else if (valorTotalItens <= 5000) {
            return 0.15;
        } else {
            return 0.20;
        }
    }

    private Double calcularAliquotaLucroPresumido(double valorTotalItens) {
        if (valorTotalItens < 1000) {
            return 0.03;
        } else if (valorTotalItens <= 2000) {
            return 0.09;
        } else if (valorTotalItens <= 5000) {
            return 0.16;
        } else {
            return 0.20;
        }
    }

    private List<ItemNotaFiscal> calcularItens(List<Item> itens, Double aliquota) {
        return itens.stream()
                .map(item -> ItemNotaFiscal.builder()
                        .idItem(item.getIdItem())
                        .descricao(item.getDescricao())
                        .valorUnitario(item.getValorUnitario())
                        .quantidade(item.getQuantidade())
                        .valorTributoItem(item.getValorUnitario() * aliquota)
                        .build())
                .collect(Collectors.toList());
    }
}



