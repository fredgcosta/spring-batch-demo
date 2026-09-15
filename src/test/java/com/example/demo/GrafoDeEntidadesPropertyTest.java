package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.models.Transaction;
import com.example.demo.support.FixedWidthInputGenerator;
import com.example.demo.support.FixedWidthInputGenerator.FieldTriple;
import com.example.demo.support.FixedWidthInputGenerator.TransactionSpec;
import com.example.demo.support.SouJavaJobJqwikSupport;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.lifecycle.BeforeContainer;

/**
 * Teste de PROPRIEDADE (jqwik) do {@code souJavaJob}.
 *
 * <p>
 * // Feature: java-25-upgrade, Property 1: Processamento do job produz o grafo
 * de entidades esperado
 *
 * <p>
 * <strong>Validates: Requirements 6.5</strong>
 *
 * <p>
 * Para qualquer arquivo de entrada de largura fixa válido contendo N blocos de
 * transação ({@code 0100}+{@code 0101}+{@code 0102}+{@code 0103}), a execução
 * do
 * {@code souJavaJob} deve persistir exatamente N entidades {@code Transaction}
 * e, para
 * cada uma, devem existir {@code RegTypeOne/Two/Three} compartilhando a MESMA
 * chave
 * primária (o id da {@code Transaction}).
 *
 * <p>
 * Integração jqwik+Spring e isolamento de estado: ver
 * {@link SouJavaJobJqwikSupport}. Cada tentativa começa limpando a base H2 para
 * que a
 * contagem seja exata (o contexto/base são reutilizados entre as tentativas,
 * pois o
 * jqwik não reinicia o ciclo de vida do Spring por tentativa).
 */
class GrafoDeEntidadesPropertyTest {

    @BeforeContainer
    static void subirContexto() {
        SouJavaJobJqwikSupport.iniciarContextoSeNecessario();
    }

    // Validates: Requirements 6.5
    // @formatter:off
    // Feature: java-25-upgrade, Property 1: Processamento do job produz o grafo de entidades esperado
    // @formatter:on
    @Property(tries = 100)
    void processamentoProduzGrafoDeEntidadesEsperado(
            @ForAll @IntRange(min = 1, max = 10) int numeroDeTransacoes,
            @ForAll @LongRange(min = 1L, max = 900_000_000_000L) long baseId,
            @ForAll("campo") String campo) {

        // Isolamento de estado: base limpa no início de CADA tentativa.
        SouJavaJobJqwikSupport.limparBase();

        // Ids positivos, únicos dentro do arquivo e <= 18 dígitos (base + índice).
        List<Long> ids = new ArrayList<>(numeroDeTransacoes);
        List<TransactionSpec> specs = new ArrayList<>(numeroDeTransacoes);
        for (int i = 0; i < numeroDeTransacoes; i++) {
            long id = baseId + i;
            ids.add(id);
            FieldTriple triple = new FieldTriple(campo, campo, campo);
            specs.add(new TransactionSpec(id, campo, campo, campo, triple, triple, triple));
        }

        String conteudo = FixedWidthInputGenerator.gerarConteudo(
                specs,
                FixedWidthInputGenerator.DEFAULT_HEADER_TEXT,
                FixedWidthInputGenerator.DEFAULT_FOOTER_TEXT);

        JobExecution execucao = SouJavaJobJqwikSupport.executarJob(conteudo);

        assertThat(execucao.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // (a) count(Transaction) == N (== quantidade de linhas 0100)
        assertThat(SouJavaJobJqwikSupport.transactionRepository().count())
                .isEqualTo(numeroDeTransacoes);

        // (b) para cada Transaction persistida, existem RegTypeOne/Two/Three com o
        // MESMO id.
        for (Long id : ids) {
            assertThat(SouJavaJobJqwikSupport.transactionRepository().findById(id))
                    .as("Transaction id=%s deve existir", id)
                    .isPresent();
            assertThat(SouJavaJobJqwikSupport.regTypeOneRepository().existsById(id))
                    .as("RegTypeOne deve compartilhar o id %s da Transaction", id)
                    .isTrue();
            assertThat(SouJavaJobJqwikSupport.regTypeTwoRepository().existsById(id))
                    .as("RegTypeTwo deve compartilhar o id %s da Transaction", id)
                    .isTrue();
            assertThat(SouJavaJobJqwikSupport.regTypeThreeRepository().existsById(id))
                    .as("RegTypeThree deve compartilhar o id %s da Transaction", id)
                    .isTrue();
        }

        // Confirma também a associação carregada (id compartilhado via @MapsId) dentro
        // de sessão.
        SouJavaJobJqwikSupport.emTransacaoDeLeitura(() -> {
            for (Transaction t : SouJavaJobJqwikSupport.transactionRepository().findAll()) {
                assertThat(t.getRegTypeOne().getId()).isEqualTo(t.getId());
                assertThat(t.getRegTypeTwo().getId()).isEqualTo(t.getId());
                assertThat(t.getRegTypeThree().getId()).isEqualTo(t.getId());
            }
            return null;
        });
    }

    /**
     * Valores de campo "7-char-safe": alfanuméricos, sem espaços nas bordas (o
     * tokenizer
     * de largura fixa faz trim), de 1 a 12 chars (o gerador ajusta para a largura
     * 7).
     */
    @net.jqwik.api.Provide
    net.jqwik.api.Arbitrary<String> campo() {
        return net.jqwik.api.Arbitraries.strings()
                .withCharRange('a', 'z')
                .withChars('0', '9')
                .ofMinLength(1)
                .ofMaxLength(12);
    }
}
