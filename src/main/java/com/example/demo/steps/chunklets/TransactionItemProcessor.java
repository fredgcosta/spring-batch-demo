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
        item.setField04(item.getField04().toUpperCase());
        item.setField05(item.getField05().toUpperCase());

        item.getRegTypeOne().setField11(item.getRegTypeOne().getField11().toUpperCase());
        item.getRegTypeOne().setField12(item.getRegTypeOne().getField12().toUpperCase());
        item.getRegTypeOne().setField13(item.getRegTypeOne().getField13().toUpperCase());
        item.getRegTypeOne().setField14(item.getRegTypeOne().getField14().toUpperCase());
        item.getRegTypeOne().setField15(item.getRegTypeOne().getField15().toUpperCase());

        item.getRegTypeTwo().setField21(item.getRegTypeTwo().getField21().toUpperCase());
        item.getRegTypeTwo().setField22(item.getRegTypeTwo().getField22().toUpperCase());
        item.getRegTypeTwo().setField23(item.getRegTypeTwo().getField23().toUpperCase());
        item.getRegTypeTwo().setField24(item.getRegTypeTwo().getField24().toUpperCase());
        item.getRegTypeTwo().setField25(item.getRegTypeTwo().getField25().toUpperCase());

        item.getRegTypeThree().setField31(item.getRegTypeThree().getField31().toUpperCase());
        item.getRegTypeThree().setField32(item.getRegTypeThree().getField32().toUpperCase());
        item.getRegTypeThree().setField33(item.getRegTypeThree().getField33().toUpperCase());
        item.getRegTypeThree().setField34(item.getRegTypeThree().getField34().toUpperCase());
        item.getRegTypeThree().setField35(item.getRegTypeThree().getField35().toUpperCase());

        log.info(item.toString());
        return item;
    }

}
