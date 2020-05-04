package com.example.demo.steps.mappers;

import com.example.demo.models.Transaction;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class TransactionFieldSetMapper implements FieldSetMapper<Transaction> {
    public static final String ID_COLUMN = "id";
    public static final String FIELD01_COLUMN = "field01";
    public static final String FIELD02_COLUMN = "field02";
    public static final String FIELD03_COLUMN = "field03";
    public static final String FIELD04_COLUMN = "field04";
    public static final String FIELD05_COLUMN = "field05";

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {
        Transaction transaction = new Transaction();
        transaction.setId(fieldSet.readLong(ID_COLUMN));
        transaction.setField01(fieldSet.readString(FIELD01_COLUMN));
        transaction.setField02(fieldSet.readString(FIELD02_COLUMN));
        transaction.setField03(fieldSet.readString(FIELD02_COLUMN));
        transaction.setField04(fieldSet.readString(FIELD02_COLUMN));
        transaction.setField05(fieldSet.readString(FIELD02_COLUMN));
        return transaction;
    }

}
