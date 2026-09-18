package com.example.demo.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.example.demo.SpringBatchDemoApplication;
import com.example.demo.repositories.TransactionRepository;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Suporte compartilhado pelos testes de PROPRIEDADE (jqwik) do
 * {@code souJavaJob}.
 *
 * <p>
 * <strong>Por que não {@code @SpringBootTest}?</strong> Os métodos
 * {@code @Property}
 * do jqwik não passam pelo ciclo de vida do Spring (a extensão
 * {@code SpringExtension}
 * do JUnit Jupiter não é aplicada pelo engine do jqwik, e o módulo
 * {@code jqwik-spring}
 * não está no classpath). Assim, campos {@code @Autowired} ficariam nulos
 * dentro de um
 * {@code @Property}. Para obter uma integração jqwik+Spring confiável e
 * não-flaky, este
 * suporte sobe o contexto Spring Boot completo UMA ÚNICA VEZ (lazy, estático)
 * via
 * {@link SpringApplicationBuilder} e expõe os beans necessários. As classes de
 * teste de
 * propriedade chamam este suporte a partir de
 * {@code @BeforeContainer}/{@code @Property}.
 *
 * <p>
 * O contexto usa {@code src/test/resources/application.properties} (H2 em
 * memória,
 * OTLP desabilitado) e desliga a execução automática do job no startup
 * ({@code spring.batch.job.enabled=false}), de modo que o job só roda quando o
 * teste o
 * dispara explicitamente via {@link #executarJob(String)}.
 *
 * <p>
 * <strong>Isolamento de estado:</strong> como o mesmo contexto (e a mesma base
 * H2) é
 * reutilizado por todas as iterações/tentativas, os testes DEVEM chamar
 * {@link #limparBase()} no início de cada tentativa para garantir contagens
 * exatas.
 */
public final class SouJavaJobJqwikSupport {

    /** Diretório base (classpath) do reader, casando o default do job. */
    public static final String BASE_DIR = "/input/";

    private static volatile ConfigurableApplicationContext context;

    private static JobLauncher jobLauncher;
    private static Job souJavaJob;
    private static TransactionRepository transactionRepository;
    private static RegTypeOneTestRepository regTypeOneRepository;
    private static RegTypeTwoTestRepository regTypeTwoRepository;
    private static RegTypeThreeTestRepository regTypeThreeRepository;
    private static TransactionTemplate transactionTemplate;

    private SouJavaJobJqwikSupport() {
        // utilitário estático
    }

    /**
     * Sobe o contexto Spring Boot uma única vez para toda a JVM de teste
     * (idempotente).
     * Seguro para ser chamado de vários {@code @BeforeContainer} de classes
     * distintas.
     */
    public static synchronized void iniciarContextoSeNecessario() {
        if (context != null) {
            return;
        }
        context = new SpringApplicationBuilder(SpringBatchDemoApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        // Não executar o job automaticamente no startup: os testes o disparam.
                        "spring.batch.job.enabled=false",
                        "spring.cloud.task.batch.fail-on-job-failure=false")
                .run();

        jobLauncher = context.getBean(JobLauncher.class);
        souJavaJob = context.getBean("souJavaJob", Job.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        regTypeOneRepository = context.getBean(RegTypeOneTestRepository.class);
        regTypeTwoRepository = context.getBean(RegTypeTwoTestRepository.class);
        regTypeThreeRepository = context.getBean(RegTypeThreeTestRepository.class);
        // Há duas beans PlatformTransactionManager (JPA + Spring Cloud Task); usar a
        // JPA por nome.
        transactionTemplate = new TransactionTemplate(
                context.getBean("transactionManager",
                        org.springframework.transaction.PlatformTransactionManager.class));
    }

    /**
     * Remove todas as {@code Transaction} (e, por cascata, os {@code RegType*}) da
     * base H2.
     */
    public static void limparBase() {
        transactionRepository.deleteAll();
        // Garantia defensiva: se algum órfão restar (sem orphanRemoval configurado),
        // limpa também.
        regTypeOneRepository.deleteAll();
        regTypeTwoRepository.deleteAll();
        regTypeThreeRepository.deleteAll();
    }

    /**
     * Escreve {@code conteudo} em um arquivo único sob
     * {@code target/test-classes/input/}
     * e dispara o {@code souJavaJob} sobre ele, devolvendo a {@link JobExecution}
     * resultante.
     */
    public static JobExecution executarJob(String conteudo) {
        String fileName = escreverEntradaNoClasspath(conteudo);
        return executarJobComArquivo(fileName);
    }

    /**
     * Dispara o {@code souJavaJob} sobre um arquivo já presente no classpath de
     * teste.
     */
    public static JobExecution executarJobComArquivo(String fileName) {
        JobParameters parametros = new JobParametersBuilder()
                .addString("baseDir", BASE_DIR)
                .addString("fileName", fileName)
                .addLong("execId", System.nanoTime())
                .toJobParameters();
        try {
            // JobLauncher.run respeita EXATAMENTE os parâmetros informados (sem aplicar o
            // RunIdIncrementer), garantindo que o parâmetro fileName seja honrado pelo
            // reader
            // @StepScope. O parâmetro execId único torna cada execução uma nova instância.
            return jobLauncher.run(souJavaJob, parametros);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao disparar o souJavaJob para " + fileName, e);
        }
    }

    /**
     * Escreve o conteúdo informado em um arquivo único no classpath de teste e
     * devolve o nome.
     */
    public static String escreverEntradaNoClasspath(String conteudo) {
        String fileName = "prop-" + UUID.randomUUID() + ".txt";
        Path destino = diretorioInputDoClasspath().resolve(fileName);
        try {
            Files.writeString(destino, conteudo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao escrever entrada de teste em " + destino, e);
        }
        return fileName;
    }

    public static TransactionRepository transactionRepository() {
        return transactionRepository;
    }

    public static RegTypeOneTestRepository regTypeOneRepository() {
        return regTypeOneRepository;
    }

    public static RegTypeTwoTestRepository regTypeTwoRepository() {
        return regTypeTwoRepository;
    }

    public static RegTypeThreeTestRepository regTypeThreeRepository() {
        return regTypeThreeRepository;
    }

    /**
     * Executa uma leitura dentro de uma transação (necessário para navegar
     * associações LAZY).
     */
    public static <T> T emTransacaoDeLeitura(java.util.function.Supplier<T> leitura) {
        return transactionTemplate.execute(status -> leitura.get());
    }

    private static Path diretorioInputDoClasspath() {
        URL raiz = SouJavaJobJqwikSupport.class.getClassLoader().getResource("");
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
