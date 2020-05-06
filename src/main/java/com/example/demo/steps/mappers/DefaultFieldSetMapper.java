package com.example.demo.steps.mappers;

import com.example.demo.models.Transaction;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class DefaultFieldSetMapper implements FieldSetMapper<Transaction> {
    public static final String REG_ID = "regId";

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {
        Transaction transaction = new Transaction();
        transaction.setRegId(fieldSet.readString(REG_ID));
        return transaction;
    }
}