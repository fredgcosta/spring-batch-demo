package com.example.demo.steps.chunklets;

import java.util.List;

import javax.transaction.Transactional;

import com.example.demo.models.Transaction;
import com.example.demo.repositories.TransactionRepository;

import org.springframework.batch.item.ItemWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemWriter implements ItemWriter<Transaction> {
    private TransactionRepository transactionRepository;

    public TransactionItemWriter(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void write(List<? extends Transaction> items) throws Exception {
        log.info("salvando a lista de transações em lotes de " + items.size());
        transactionRepository.saveAll(items);
    }

}