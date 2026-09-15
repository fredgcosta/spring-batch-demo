package com.example.demo.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.example.demo.repositories.TransactionRepository;

import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base/harness de teste para exercitar o {@code souJavaJob} sobre H2 em memória,
 * com exportação OTLP desabilitada (via {@code src/test/resources/application.properties}).
 *
 * <p>Fornece a infraestrutura comum usada pelos testes de propriedade (tarefas
 * 8.2/8.3) e pelo teste de edge case (8.4):
 * <ul>
 *   <li>Sobe o contexto Spring Boot completo (o job {@code souJavaJob} e seus beans).</li>
 *   <li>Expõe o {@link JobLauncherTestUtils} para disparar o job.</li>
 *   <li>Expõe o {@link TransactionRepository} para asserções sobre as entidades persistidas.</li>
 *   <li>Escreve o conteúdo de entrada gerado em um recurso de classpath
 *       ({@code target/test-classes/input/}) que o reader do job resolve via
 *       {@code ClassPathResource(baseDir + fileName)}.</li>
 * </ul>
 *
 * <p>O reader do job é {@code @StepScope} e lê os parâmetros {@code baseDir} e
 * {@code fileName} via SpEL; por isso este harness passa {@code baseDir=/input/}
 * e um {@code fileName} único por execução, evitando colisão com o arquivo padrão
 * {@code exemplo-sou-java-10.txt}.
 *
 * <p>Subclasses devem herdar desta classe (que já traz {@link SpringBootTest} e
 * {@link SpringBatchTest}) para obter os beans autowired e os helpers.
 */
@SpringBootTest
@SpringBatchTest
public abstract class SouJavaJobTestHarness {

    /** Diretório base (classpath) do reader, casando o default do job. */
    protected static final String BASE_DIR = "/input/";

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    protected TransactionRepository transactionRepository;

    /**
     * Escreve o conteúdo informado em um arquivo único sob {@code target/test-classes/input/}
     * (raiz de classpath de teste) e devolve o nome do arquivo, pronto para ser
     * passado como parâmetro {@code fileName} do job.
     *
     * @param conteudo o conteúdo de largura fixa a ser gravado
     * @return o nome do arquivo gerado (relativo a {@link #BASE_DIR})
     */
    protected String escreverEntradaNoClasspath(String conteudo) {
        String fileName = "gerado-" + UUID.randomUUID() + ".txt";
        Path destino = diretorioInputDoClasspath().resolve(fileName);
        try {
            Files.writeString(destino, conteudo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao escrever entrada de teste em " + destino, e);
        }
        return fileName;
    }

    /**
     * Monta os {@link JobParameters} do {@code souJavaJob} para um arquivo de
     * entrada específico. Inclui um parâmetro de identificação único para permitir
     * reexecuções na mesma JVM (o {@code RunIdIncrementer} também contribui).
     */
    protected JobParameters parametrosPara(String fileName) {
        return new JobParametersBuilder()
                .addString("baseDir", BASE_DIR)
                .addString("fileName", fileName)
                .addLong("execId", System.nanoTime())
                .toJobParameters();
    }

    /**
     * Resolve o diretório físico {@code input/} na raiz do classpath de teste
     * ({@code target/test-classes/input/}), criando-o se necessário.
     */
    private Path diretorioInputDoClasspath() {
        URL raiz = getClass().getClassLoader().getResource("");
        if (raiz == null) {
            throw new IllegalStateException("Raiz de classpath de teste não encontrada");
        }
        try {
            Path input = Path.of(raiz.toURI()).resolve("input");
            Files.createDirectories(input);
            return input;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao resolver o diretório input do classpath", e);
        }
    }
}
