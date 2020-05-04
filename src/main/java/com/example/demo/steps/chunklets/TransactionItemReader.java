package com.example.demo.steps.chunklets;

import com.example.demo.models.RegTypeOne;
import com.example.demo.models.RegTypeThree;
import com.example.demo.models.RegTypeTwo;
import com.example.demo.models.Transaction;
import com.example.demo.steps.mappers.RegTypeOneFieldSetMapper;
import com.example.demo.steps.mappers.RegTypeThreeFieldSetMapper;
import com.example.demo.steps.mappers.RegTypeTwoFieldSetMapper;
import com.example.demo.steps.mappers.TransactionFieldSetMapper;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.BindException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionItemReader implements ItemReader<Transaction> {

    private boolean recordFinished;
    private ItemReader<FieldSet> fieldSetReader;
    private Transaction transaction;
    private Transaction transactionTmp;

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        recordFinished = false;

        while (!recordFinished) {
            process(fieldSetReader.read());
        }

        Transaction result = transaction;
        transaction = null;

        return result;
    }

    private void process(FieldSet fieldSet) throws BindException {

        // finish processing if we hit the end of file
        if (fieldSet == null) {
            transaction = transactionTmp;
            transactionTmp = null;
            recordFinished = true;
            return;
        }
        log.info("Lendo os primeiros campos do Registro");
        String lineId = fieldSet.readString(0) + fieldSet.readString(1);

        switch (lineId) {
            case Transaction.LINE_ID:
                if (transactionTmp == null) {
                    log.info("início de um registro de transação");
                    transactionTmp = transactionMapper().mapFieldSet(fieldSet);
                } else {
                    log.info("fim de um registro de transação");
                    transaction = transactionTmp;
                    transactionTmp = transactionMapper().mapFieldSet(fieldSet);
                    recordFinished = true;
                }
                break;
            case RegTypeOne.LINE_ID:
                RegTypeOne regTypeOne = regTypeOneMapper().mapFieldSet(fieldSet);
                regTypeOne.setId(transactionTmp.getId());
                regTypeOne.setTransaction(transactionTmp);
                transactionTmp.setRegTypeOne(regTypeOne);
                break;
            case RegTypeTwo.LINE_ID:
                RegTypeTwo regTypeTwo = regTypeTwoMapper().mapFieldSet(fieldSet);
                regTypeTwo.setId(transactionTmp.getId());
                regTypeTwo.setTransaction(transactionTmp);
                transactionTmp.setRegTypeTwo(regTypeTwo);
                break;
            case RegTypeThree.LINE_ID:
                RegTypeThree regTypeThree = regTypeThreeMapper().mapFieldSet(fieldSet);
                regTypeThree.setId(transactionTmp.getId());
                regTypeThree.setTransaction(transactionTmp);
                transactionTmp.setRegTypeThree(regTypeThree);
                break;
            default:
                log.warn("Tipo de registro não reconhecido");
                break;
        }
    }

    public void setFieldSetReader(ItemReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @Bean
    public TransactionFieldSetMapper transactionMapper() {
        return new TransactionFieldSetMapper();
    }

    @Bean
    public RegTypeOneFieldSetMapper regTypeOneMapper() {
        return new RegTypeOneFieldSetMapper();
    }

    @Bean
    public RegTypeTwoFieldSetMapper regTypeTwoMapper() {
        return new RegTypeTwoFieldSetMapper();
    }

    @Bean
    public RegTypeThreeFieldSetMapper regTypeThreeMapper() {
        return new RegTypeThreeFieldSetMapper();
    }
}
