package com.example.demo.steps.chunklets;

import org.springframework.transaction.annotation.Transactional;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemWriter implements ItemWriter<Transaction> {
    private TransactionRepository transactionRepository;

    public TransactionItemWriter(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

        @Override

        @Transactional("transactionManager")
        public void write(Chunk<? extends Transaction> items) throws Exception {
                log.info("salvando a lista de transações em lotes de " + items.getItems().size());
                transactionRepository.saveAll(items);
        }

}