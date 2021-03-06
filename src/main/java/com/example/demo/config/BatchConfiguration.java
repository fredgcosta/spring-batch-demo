package com.example.demo.config;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.steps.chunklets.TransactionItemProcessor;
import com.example.demo.steps.chunklets.TransactionItemWriter;
import com.example.demo.steps.mappers.DefaultCompositeLineMapper;
import com.example.demo.steps.mappers.DefaultRecordSeparationPolicy;
import com.example.demo.steps.tasklets.FileDownloadTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableTask
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

  private static final int CHUNK_SIZE = 2500;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

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
  public Step chunkletStep() {
    return stepBuilderFactory.get("transactionProcessingStep")
        .<Transaction, Transaction> chunk(CHUNK_SIZE)
        .reader(reader(null, null))
        .processor(processor())
        .writer(writer(null))
        .stream(reader(null, null)).build();
  }

  @Bean
  public Step taskletStep() {
    return stepBuilderFactory.get("fileDownloadingStep").tasklet(new FileDownloadTasklet()).build();
  }

  @Bean
  public Job souJavaJob(@Autowired JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("souJavaJob")
        .incrementer(new RunIdIncrementer())
        .start(taskletStep())
        .next(chunkletStep())
        .build();
  }
}