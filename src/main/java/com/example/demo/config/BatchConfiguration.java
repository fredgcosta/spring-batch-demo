package com.example.demo.config;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.steps.chunklets.TransactionItemProcessor;
import com.example.demo.steps.chunklets.TransactionItemWriter;
import com.example.demo.steps.mappers.DefaultCompositeLineMapper;
import com.example.demo.steps.mappers.DefaultRecordSeparationPolicy;
import com.example.demo.steps.tasklets.FileDownloadTasklet;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableTask
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

  private static final int CHUNK_SIZE = 2500;

  @Bean
  @StepScope
  public FlatFileItemReader<Transaction> reader(
      @Value("#{jobParameters['baseDir'] ?: '/input/'}") final String baseDir,
      @Value("#{jobParameters['fileName'] ?: 'exemplo-sou-java-10.txt' }") final String fileName) {

    return new FlatFileItemReaderBuilder<Transaction>()
        .name("myFlatFileItemReader")
        .resource(new ClassPathResource(baseDir + fileName))
        .lineMapper(new DefaultCompositeLineMapper())
        .recordSeparatorPolicy(new DefaultRecordSeparationPolicy())
        .linesToSkip(1)
        .skippedLinesCallback(log::info)
        .build();
  }

  @Bean
  @StepScope
  public TransactionItemProcessor processor() {
    return new TransactionItemProcessor();
  }

  @Bean
  @StepScope
  public TransactionItemWriter writer(@Autowired TransactionRepository transactionRepository) {
    return new TransactionItemWriter(transactionRepository);
  }

  @Bean
  public Step chunkletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("transactionProcessingStep", jobRepository)
        .<Transaction, Transaction> chunk(CHUNK_SIZE)
        .transactionManager(transactionManager)
        .reader(reader(null, null))
        .processor(processor())
        .writer(writer(null))
        .stream(reader(null, null)).build();
  }

  @Bean
  public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("fileDownloadingStep", jobRepository).tasklet(new FileDownloadTasklet(), transactionManager).build();
  }

  @Bean
  public Job souJavaJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new JobBuilder("souJavaJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(taskletStep(jobRepository, transactionManager))
        .next(chunkletStep(jobRepository, transactionManager))
        .build();
  }
}