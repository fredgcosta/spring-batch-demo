package com.example.demo.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Teste unitário leve do {@link FixedWidthInputGenerator}: NÃO sobe contexto
 * Spring nem executa o job, servindo apenas para provar (de forma não-flaky) que
 * o gerador produz linhas com as larguras fixas esperadas pelo
 * {@code TransactionTokenizer} e no mesmo layout do arquivo de exemplo.
 *
 * <p>A execução real do {@code souJavaJob} sobre a entrada gerada é coberta pelas
 * tarefas 8.2/8.3/8.4 usando o {@link SouJavaJobTestHarness}.
 */
class FixedWidthInputGeneratorTest {

    @Test
    void gerarConteudoProduzCabecalhoBlocosERodape() {
        String conteudo = FixedWidthInputGenerator.gerarConteudo(3);
        List<String> linhas = conteudo.lines().toList();

        // 1 cabeçalho + 3 blocos * 4 linhas + 1 rodapé = 14 linhas
        assertThat(linhas).hasSize(1 + 3 * 4 + 1);
        assertThat(linhas.get(0)).startsWith(FixedWidthInputGenerator.HEADER_REG_ID);
        assertThat(linhas.get(linhas.size() - 1)).startsWith(FixedWidthInputGenerator.FOOTER_REG_ID);

        // Primeiro bloco: prefixos das quatro linhas físicas
        assertThat(linhas.get(1)).startsWith(FixedWidthInputGenerator.TRANSACTION_REG_ID);
        assertThat(linhas.get(2)).startsWith(FixedWidthInputGenerator.REG_TYPE_ONE_REG_ID);
        assertThat(linhas.get(3)).startsWith(FixedWidthInputGenerator.REG_TYPE_TWO_REG_ID);
        assertThat(linhas.get(4)).startsWith(FixedWidthInputGenerator.REG_TYPE_THREE_REG_ID);
    }

    @Test
    void linhaDeTransacaoTemLarguraEIdZeroPadded() {
        String conteudo = FixedWidthInputGenerator.gerarConteudo(1);
        List<String> linhas = conteudo.lines().toList();

        String linha0100 = linhas.get(1);
        // 0100 (4) + id (18) + 3 campos (7 cada) = 43 chars
        int larguraEsperada = FixedWidthInputGenerator.REG_ID_WIDTH
                + FixedWidthInputGenerator.TRANSACTION_ID_WIDTH
                + 3 * FixedWidthInputGenerator.FIELD_WIDTH;
        assertThat(linha0100).hasSize(larguraEsperada);
        // Espelha o layout do arquivo de exemplo para o id 1
        assertThat(linha0100).isEqualTo("0100000000000000000001campo03campo04campo05");

        String linha0101 = linhas.get(2);
        // 0101 (4) + 3 campos (7 cada) = 25 chars
        int larguraSub = FixedWidthInputGenerator.REG_ID_WIDTH + 3 * FixedWidthInputGenerator.FIELD_WIDTH;
        assertThat(linha0101).hasSize(larguraSub);
        assertThat(linha0101).isEqualTo("0101campo13campo14campo15");
    }

    @Test
    void camposSaoAjustadosParaLarguraFixa() {
        // campo mais curto que 7 -> preenchido com espaços; mais longo -> truncado
        var spec = new FixedWidthInputGenerator.TransactionSpec(
                42L,
                "ab", "1234567890", "c",
                new FixedWidthInputGenerator.FieldTriple("x", "y", "z"),
                new FixedWidthInputGenerator.FieldTriple("x", "y", "z"),
                new FixedWidthInputGenerator.FieldTriple("x", "y", "z"));

        String conteudo = FixedWidthInputGenerator.gerarConteudo(List.of(spec), "h", "f");
        String linha0100 = conteudo.lines().toList().get(1);

        assertThat(linha0100).startsWith("0100000000000000000042");
        // "ab" -> "ab     " (7), "1234567890" -> "1234567" (7 truncado), "c" -> "c      " (7)
        assertThat(linha0100).isEqualTo("0100000000000000000042ab     1234567c      ");
    }

    @Test
    void zeroTransacoesProduzApenasCabecalhoERodape() {
        String conteudo = FixedWidthInputGenerator.gerarConteudo(0);
        List<String> linhas = conteudo.lines().toList();
        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(0)).startsWith(FixedWidthInputGenerator.HEADER_REG_ID);
        assertThat(linhas.get(1)).startsWith(FixedWidthInputGenerator.FOOTER_REG_ID);
    }
}
