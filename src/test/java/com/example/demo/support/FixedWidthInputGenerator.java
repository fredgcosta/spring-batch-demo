package com.example.demo.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerador programático de arquivos de entrada de largura fixa VÁLIDOS para o
 * {@code souJavaJob}, espelhando exatamente o layout de
 * {@code src/main/resources/input/exemplo-sou-java-10.txt} e as larguras de
 * coluna definidas nos tokenizers (ver
 * {@code com.example.demo.steps.tokenizers.TransactionTokenizer}).
 *
 * <p>Cada registro lógico de transação ocupa quatro linhas físicas que a
 * {@code DefaultRecordSeparationPolicy} concatena em um único registro de 118
 * caracteres consumido pelo {@code TransactionTokenizer}:
 *
 * <pre>
 * ----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5--
 * 0100000000000000000001campo03campo04campo050101campo13campo14campo150102campo23campo24campo250103campo33campo34campo35
 * |--0100--||----id 18----||-f01--||-f02--||-f03--||0101||-f01--||-f02--||-f03--||0102||...||0103||...           |
 * </pre>
 *
 * Larguras (a partir das {@code Range}s do {@code TransactionTokenizer}):
 * <ul>
 *   <li>regId de cada linha: {@value #REG_ID_WIDTH} chars</li>
 *   <li>id da transação (linha 0100): {@value #TRANSACTION_ID_WIDTH} chars (numérico, zero-padded)</li>
 *   <li>cada campo de dados: {@value #FIELD_WIDTH} chars</li>
 * </ul>
 *
 * <p>Um arquivo gerado tem: um cabeçalho {@code 0000}, N blocos de transação
 * ({@code 0100}+{@code 0101}+{@code 0102}+{@code 0103}) e um rodapé {@code 9999}.
 * O reader do job pula a primeira linha ({@code linesToSkip(1)} — o cabeçalho).
 *
 * <p>Classe utilitária de teste: sem estado, apenas fábricas estáticas.
 */
public final class FixedWidthInputGenerator {

    /** Largura do campo regId (prefixo) de cada linha física. */
    public static final int REG_ID_WIDTH = 4;
    /** Largura do id da transação na linha 0100 (numérico, zero-padded). */
    public static final int TRANSACTION_ID_WIDTH = 18;
    /** Largura de cada campo de dados. */
    public static final int FIELD_WIDTH = 7;

    public static final String HEADER_REG_ID = "0000";
    public static final String TRANSACTION_REG_ID = "0100";
    public static final String REG_TYPE_ONE_REG_ID = "0101";
    public static final String REG_TYPE_TWO_REG_ID = "0102";
    public static final String REG_TYPE_THREE_REG_ID = "0103";
    public static final String FOOTER_REG_ID = "9999";

    public static final String DEFAULT_HEADER_TEXT = "este eh o header do arquivo";
    public static final String DEFAULT_FOOTER_TEXT = "este eh o footer do arquivo";

    private static final String LINE_SEPARATOR = "\n";

    private FixedWidthInputGenerator() {
        // utilitário
    }

    /**
     * Gera o conteúdo de um arquivo de entrada válido com {@code numeroDeTransacoes}
     * blocos de transação, usando textos de cabeçalho/rodapé padrão e valores de
     * campo determinísticos (derivados do índice do bloco).
     *
     * @param numeroDeTransacoes quantidade de registros lógicos de transação (N &gt;= 0)
     * @return o conteúdo completo do arquivo (linhas separadas por {@code \n})
     */
    public static String gerarConteudo(int numeroDeTransacoes) {
        List<TransactionSpec> specs = new ArrayList<>(Math.max(0, numeroDeTransacoes));
        for (int i = 1; i <= numeroDeTransacoes; i++) {
            specs.add(TransactionSpec.padrao(i));
        }
        return gerarConteudo(specs, DEFAULT_HEADER_TEXT, DEFAULT_FOOTER_TEXT);
    }

    /**
     * Gera o conteúdo de um arquivo de entrada válido a partir de especificações
     * explícitas de transação, permitindo que os testes controlem os valores de
     * campo (útil para os testes de propriedade das tarefas 8.2/8.3).
     *
     * @param transacoes especificações dos blocos de transação (uma por bloco)
     * @param textoCabecalho texto livre do cabeçalho {@code 0000}
     * @param textoRodape texto livre do rodapé {@code 9999}
     * @return o conteúdo completo do arquivo (linhas separadas por {@code \n})
     */
    public static String gerarConteudo(List<TransactionSpec> transacoes, String textoCabecalho, String textoRodape) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER_REG_ID).append(textoCabecalho).append(LINE_SEPARATOR);
        for (TransactionSpec t : transacoes) {
            sb.append(linhaTransacao(t)).append(LINE_SEPARATOR);
            sb.append(linhaSubRegistro(REG_TYPE_ONE_REG_ID, t.regTypeOne())).append(LINE_SEPARATOR);
            sb.append(linhaSubRegistro(REG_TYPE_TWO_REG_ID, t.regTypeTwo())).append(LINE_SEPARATOR);
            sb.append(linhaSubRegistro(REG_TYPE_THREE_REG_ID, t.regTypeThree())).append(LINE_SEPARATOR);
        }
        sb.append(FOOTER_REG_ID).append(textoRodape);
        return sb.toString();
    }

    /** Monta a linha {@code 0100}: regId + id (18, zero-padded) + 3 campos (7 cada). */
    private static String linhaTransacao(TransactionSpec t) {
        return TRANSACTION_REG_ID
                + padId(t.id())
                + padCampo(t.field01())
                + padCampo(t.field02())
                + padCampo(t.field03());
    }

    /** Monta uma linha de sub-registro ({@code 0101}/{@code 0102}/{@code 0103}): regId + 3 campos (7 cada). */
    private static String linhaSubRegistro(String regId, FieldTriple campos) {
        return regId
                + padCampo(campos.field01())
                + padCampo(campos.field02())
                + padCampo(campos.field03());
    }

    /** Zero-pad à esquerda do id numérico da transação para {@value #TRANSACTION_ID_WIDTH} chars. */
    private static String padId(long id) {
        String raw = Long.toString(id);
        if (raw.length() > TRANSACTION_ID_WIDTH) {
            throw new IllegalArgumentException(
                    "id de transação excede " + TRANSACTION_ID_WIDTH + " dígitos: " + id);
        }
        return "0".repeat(TRANSACTION_ID_WIDTH - raw.length()) + raw;
    }

    /**
     * Ajusta um valor de campo para exatamente {@value #FIELD_WIDTH} caracteres:
     * trunca se maior, completa com espaços à direita se menor. As larguras fixas
     * são obrigatórias porque os regIds dos blocos seguintes são lidos em posições
     * absolutas (44/69/94) no registro concatenado.
     */
    private static String padCampo(String valor) {
        String v = valor == null ? "" : valor;
        if (v.length() >= FIELD_WIDTH) {
            return v.substring(0, FIELD_WIDTH);
        }
        return v + " ".repeat(FIELD_WIDTH - v.length());
    }

    /** Três campos de dados de um sub-registro. */
    public record FieldTriple(String field01, String field02, String field03) {
    }

    /**
     * Especificação de um bloco de transação: o id compartilhado (linha 0100 e
     * pelos três sub-registros via {@code @MapsId}) e os três campos de cada linha.
     */
    public record TransactionSpec(
            long id,
            String field01,
            String field02,
            String field03,
            FieldTriple regTypeOne,
            FieldTriple regTypeTwo,
            FieldTriple regTypeThree) {

        /**
         * Cria uma especificação determinística padrão para o bloco de índice
         * {@code indice}, espelhando os valores {@code campoNN} do arquivo de
         * exemplo (ex.: {@code campo03}, {@code campo13}, ...).
         */
        public static TransactionSpec padrao(long indice) {
            return new TransactionSpec(
                    indice,
                    "campo03", "campo04", "campo05",
                    new FieldTriple("campo13", "campo14", "campo15"),
                    new FieldTriple("campo23", "campo24", "campo25"),
                    new FieldTriple("campo33", "campo34", "campo35"));
        }
    }
}
