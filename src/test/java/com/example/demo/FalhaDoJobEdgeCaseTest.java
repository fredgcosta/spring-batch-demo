package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.support.FixedWidthInputGenerator;
import com.example.demo.support.SouJavaJobTestHarness;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;

/**
 * Teste de EDGE CASE (JUnit Jupiter) para o caminho de FALHA do
 * {@code souJavaJob}.
 *
 * <p>
 * <strong>Requirements: 6.6</strong> — quando o job falha durante a leitura, o
 * mapeamento ou a persistência, ele deve encerrar com status de falha indicando
 * a etapa
 * responsável.
 *
 * <p>
 * Fornecemos uma entrada MALFORMADA: um bloco de transação cujo campo de id
 * (linha
 * {@code 0100}) contém caracteres não numéricos. O
 * {@code TransactionFieldSetMapper}
 * lê esse campo via {@code readLong}, o que lança durante o mapeamento e faz o
 * {@code transactionProcessingStep} falhar. O gerador padrão só emite conteúdo
 * válido,
 * por isso a malformação é construída localmente aqui.
 */
class FalhaDoJobEdgeCaseTest extends SouJavaJobTestHarness {

        /**
         * Nome da etapa de processamento (chunk) declarada no
         * {@code BatchConfiguration}.
         */
        private static final String STEP_PROCESSAMENTO = "transactionProcessingStep";

        @Test
        void jobFalhaEmEntradaMalformadaIndicandoAEtapa() throws Exception {
                String conteudoMalformado = construirEntradaComIdNaoNumerico();
                String fileName = escreverEntradaNoClasspath(conteudoMalformado);

                JobExecution execucao = jobLauncherTestUtils.getJobLauncher()
                                .run(jobLauncherTestUtils.getJob(), parametrosPara(fileName));

                // O job encerra com status de FALHA.
                assertThat(execucao.getStatus()).isEqualTo(BatchStatus.FAILED);

                // A etapa responsável (processamento) é a que falha.
                StepExecution etapaComFalha = execucao.getStepExecutions().stream()
                                .filter(se -> se.getStatus() == BatchStatus.FAILED)
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("Nenhuma etapa com status FAILED encontrada"));
                assertThat(etapaComFalha.getStepName()).isEqualTo(STEP_PROCESSAMENTO);
                assertThat(etapaComFalha.getFailureExceptions()).isNotEmpty();
        }

        /**
         * Constrói uma entrada malformada: cabeçalho válido, um bloco {@code 0100} com
         * o
         * campo de id (18 chars) preenchido com caracteres não numéricos, os três
         * sub-registros e o rodapé. O {@code readLong} sobre o id malformado provoca a
         * falha do mapeamento.
         */
        private String construirEntradaComIdNaoNumerico() {
                String idNaoNumerico = "ABCDEFGHIJKLMNOPQR"; // 18 chars, não numérico
                String campo = "campo00"; // 7 chars
                StringBuilder sb = new StringBuilder();
                sb.append(FixedWidthInputGenerator.HEADER_REG_ID)
                                .append(FixedWidthInputGenerator.DEFAULT_HEADER_TEXT).append('\n');
                // Linha 0100 malformada: 4 (regId) + 18 (id não numérico) + 3*7 (campos)
                sb.append(FixedWidthInputGenerator.TRANSACTION_REG_ID)
                                .append(idNaoNumerico)
                                .append(campo).append(campo).append(campo).append('\n');
                // Sub-registros bem formados (4 + 3*7)
                sb.append(FixedWidthInputGenerator.REG_TYPE_ONE_REG_ID)
                                .append(campo).append(campo).append(campo).append('\n');
                sb.append(FixedWidthInputGenerator.REG_TYPE_TWO_REG_ID)
                                .append(campo).append(campo).append(campo).append('\n');
                sb.append(FixedWidthInputGenerator.REG_TYPE_THREE_REG_ID)
                                .append(campo).append(campo).append(campo).append('\n');
                sb.append(FixedWidthInputGenerator.FOOTER_REG_ID)
                                .append(FixedWidthInputGenerator.DEFAULT_FOOTER_TEXT);
                return sb.toString();
        }
}
