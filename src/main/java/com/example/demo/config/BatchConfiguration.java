package com.example.demo.config;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.steps.chunklets.TransactionItemProcessor;
import com.example.demo.steps.chunklets.TransactionItemReader;
import com.example.demo.steps.chunklets.TransactionItemWriter;
import com.example.demo.steps.tasklets.FileDownloadTasklet;
import com.example.demo.steps.tokenizers.TransactionCompositeLineTokenizer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
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

  @Autowired
  private TransactionRepository transactionRepository;

  @Bean
  public FileDownloadTasklet fileDownloadTasklet() {
    return new FileDownloadTasklet();
  }

  @Bean
  public Step taskletStep() {
    return stepBuilderFactory.get("downloadFileStep").tasklet(fileDownloadTasklet()).build();
  }

  public Step chunkletStep() {
    return stepBuilderFactory.get("transactionProcessFileStep")
        .<Transaction, Transaction> chunk(CHUNK_SIZE)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .stream(flatFileItemReader(null, null)).build();
  }

  private TransactionItemReader reader() {
    TransactionItemReader reader = new TransactionItemReader();
    reader.setFieldSetReader(flatFileItemReader(null, null));
    return reader;
  }

  private TransactionItemProcessor processor() {
    return new TransactionItemProcessor();
  }

  private TransactionItemWriter writer() {
    return new TransactionItemWriter(transactionRepository);
  }

  @Bean
  public TransactionCompositeLineTokenizer compositeLineTokenizer() {
    return new TransactionCompositeLineTokenizer();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<FieldSet> flatFileItemReader(
      @Value("#{jobParameters['baseDir'] ?: '/input/'}") final String baseDir,
      @Value("#{jobParameters['fileName'] ?: 'exemplo-sou-java.txt' }") final String fileName) {

    FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
    log.info("lendo o arquivo do classpath");
    reader.setResource(new ClassPathResource(baseDir + fileName));

    reader.setLineMapper(new DefaultLineMapper<FieldSet>() {
      {
        setLineTokenizer(compositeLineTokenizer());
      }
      {
        setFieldSetMapper(new PassThroughFieldSetMapper());
      }
    });
    return reader;
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