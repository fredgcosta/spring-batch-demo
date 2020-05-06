package com.example.demo.steps.chunklets;

import com.example.demo.models.Transaction;

import org.springframework.batch.item.ItemProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemProcessor implements ItemProcessor<Transaction, Transaction> {
    @Override
    public Transaction process(Transaction item) throws Exception {
        // TODO Implementar lógica de processamento
        log.info(item.toString());
        item.setField01(item.getField01().toUpperCase());
        item.setField02(item.getField02().toUpperCase());
        item.setField03(item.getField03().toUpperCase());

        item.getRegTypeOne().setField01(item.getRegTypeOne().getField01().toUpperCase());
        item.getRegTypeOne().setField02(item.getRegTypeOne().getField02().toUpperCase());
        item.getRegTypeOne().setField03(item.getRegTypeOne().getField03().toUpperCase());

        item.getRegTypeTwo().setField01(item.getRegTypeTwo().getField01().toUpperCase());
        item.getRegTypeTwo().setField02(item.getRegTypeTwo().getField02().toUpperCase());
        item.getRegTypeTwo().setField03(item.getRegTypeTwo().getField03().toUpperCase());

        item.getRegTypeThree().setField01(item.getRegTypeThree().getField01().toUpperCase());
        item.getRegTypeThree().setField02(item.getRegTypeThree().getField02().toUpperCase());
        item.getRegTypeThree().setField03(item.getRegTypeThree().getField03().toUpperCase());

        log.info(item.toString());
        return item;
    }

}
