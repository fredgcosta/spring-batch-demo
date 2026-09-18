package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * Property 2: Preservação de comportamento entre JDK 17 e JDK 25
 * (a tag exata do design está no comentário de código sobre o método
 * {@code @Property}).
 *
 * <p>
 * <strong>Validates: Requirements 6.5</strong>
 *
 * <p>
 * Para qualquer arquivo de entrada de largura fixa válido, o grafo de entidades
 * persistido pelo {@code souJavaJob} depende APENAS da entrada (processamento
 * determinístico/estável). Isso ancora a preservação de comportamento entre JDK
 * 17 e
 * JDK 25: como o resultado é função apenas da entrada, executar a mesma entrada
 * em
 * qualquer JDK produz o mesmo grafo. A realização pragmática (conforme o
 * design) é uma
 * verificação de determinismo dentro do mesmo JDK — executamos o job DUAS vezes
 * sobre a
 * MESMA entrada (arquivos/execuções distintas, base limpa antes de cada
 * execução) e
 * exigimos grafos persistidos equivalentes; não é necessária uma execução real
 * no JDK 17.
 *
 * <p>
 * Integração jqwik+Spring e isolamento de estado: ver
 * {@link SouJavaJobJqwikSupport}.
 */
class PreservacaoDeComportamentoPropertyTest {

    @BeforeContainer
    static void subirContexto() {
        SouJavaJobJqwikSupport.iniciarContextoSeNecessario();
    }

    // Validates: Requirements 6.5
    // @formatter:off
    // Feature: java-25-upgrade, Property 2: Preservação de comportamento entre JDK 17 e JDK 25
    // @formatter:on
    @Property(tries = 100)
    void grafoPersistidoDependeApenasDaEntrada(
            @ForAll @IntRange(min = 1, max = 10) int numeroDeTransacoes,
            @ForAll @LongRange(min = 1L, max = 900_000_000_000L) long baseId,
            @ForAll("campo") String campo) {

        List<TransactionSpec> specs = new ArrayList<>(numeroDeTransacoes);
        for (int i = 0; i < numeroDeTransacoes; i++) {
            long id = baseId + i;
            FieldTriple triple = new FieldTriple(campo, campo, campo);
            specs.add(new TransactionSpec(id, campo, campo, campo, triple, triple, triple));
        }
        String conteudo = FixedWidthInputGenerator.gerarConteudo(
                specs,
                FixedWidthInputGenerator.DEFAULT_HEADER_TEXT,
                FixedWidthInputGenerator.DEFAULT_FOOTER_TEXT);

        // Execução 1 sobre entrada limpa.
        SouJavaJobJqwikSupport.limparBase();
        JobExecution execucao1 = SouJavaJobJqwikSupport.executarJob(conteudo);
        assertThat(execucao1.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Map<Long, List<String>> grafo1 = capturarGrafo();

        // Execução 2 sobre a MESMA entrada (novo arquivo/execId), com base novamente
        // limpa.
        SouJavaJobJqwikSupport.limparBase();
        JobExecution execucao2 = SouJavaJobJqwikSupport.executarJob(conteudo);
        assertThat(execucao2.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Map<Long, List<String>> grafo2 = capturarGrafo();

        // Grafos equivalentes: mesmo conjunto de ids e mesmos valores mapeados (incl.
        // RegType*).
        assertThat(grafo2).isEqualTo(grafo1);
        assertThat(grafo1).hasSize(numeroDeTransacoes);
    }

    /**
     * Captura o grafo persistido como um mapa determinístico {@code id -> valores},
     * incluindo os campos da {@code Transaction} e dos três {@code RegType*} (que
     * compartilham o id). Feito dentro de uma transação de leitura para navegar as
     * associações {@code LAZY}.
     */
    private Map<Long, List<String>> capturarGrafo() {
        return SouJavaJobJqwikSupport.emTransacaoDeLeitura(() -> {
            Map<Long, List<String>> grafo = new LinkedHashMap<>();
            for (Transaction t : SouJavaJobJqwikSupport.transactionRepository().findAll()) {
                grafo.put(t.getId(), List.of(
                        t.getField01(), t.getField02(), t.getField03(),
                        String.valueOf(t.getRegTypeOne().getId()),
                        t.getRegTypeOne().getField01(), t.getRegTypeOne().getField02(), t.getRegTypeOne().getField03(),
                        String.valueOf(t.getRegTypeTwo().getId()),
                        t.getRegTypeTwo().getField01(), t.getRegTypeTwo().getField02(), t.getRegTypeTwo().getField03(),
                        String.valueOf(t.getRegTypeThree().getId()),
                        t.getRegTypeThree().getField01(), t.getRegTypeThree().getField02(),
                        t.getRegTypeThree().getField03()));
            }
            return grafo;
        });
    }

    /** Valores de campo "7-char-safe" (ver Property 1). */
    @net.jqwik.api.Provide
    net.jqwik.api.Arbitrary<String> campo() {
        return net.jqwik.api.Arbitraries.strings()
                .withCharRange('a', 'z')
                .withChars('0', '9')
                .ofMinLength(1)
                .ofMaxLength(12);
    }
}
