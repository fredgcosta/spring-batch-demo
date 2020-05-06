package com.example.demo.steps.mappers;

import com.example.demo.models.RegTypeOne;
import com.example.demo.models.RegTypeThree;
import com.example.demo.models.RegTypeTwo;
import com.example.demo.models.Transaction;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class TransactionFieldSetMapper implements FieldSetMapper<Transaction> {
    public static final String TRANSACTION_REG_ID = "transactionRegId";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_FIELD01 = "transactionField01";
    public static final String TRANSACTION_FIELD02 = "transactionField02";
    public static final String TRANSACTION_FIELD03 = "transactionField03";
    public static final String REG_TYPE_ONE_REG_ID = "regTypeOneRegId";
    public static final String REG_TYPE_ONE_FIELD01 = "regTypeOneField01";
    public static final String REG_TYPE_ONE_FIELD02 = "regTypeOneField02";
    public static final String REG_TYPE_ONE_FIELD03 = "regTypeOneField03";
    public static final String REG_TYPE_TWO_REG_ID = "regTypeTwoRegId";
    public static final String REG_TYPE_TWO_FIELD01 = "regTypeTwoField01";
    public static final String REG_TYPE_TWO_FIELD02 = "regTypeTwoField02";
    public static final String REG_TYPE_TWO_FIELD03 = "regTypeTwoField03";
    public static final String REG_TYPE_THREE_REG_ID = "regTypeThreeRegId";
    public static final String REG_TYPE_THREE_FIELD01 = "regTypeThreeField01";
    public static final String REG_TYPE_THREE_FIELD02 = "regTypeThreeField02";
    public static final String REG_TYPE_THREE_FIELD03 = "regTypeThreeField03";

    //

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {
        Transaction transaction = new Transaction();
        Long transactionId = fieldSet.readLong(TRANSACTION_ID);
        transaction.setId(transactionId);
        transaction.setRegId(fieldSet.readString(TRANSACTION_REG_ID));
        transaction.setField01(fieldSet.readString(TRANSACTION_FIELD01));
        transaction.setField02(fieldSet.readString(TRANSACTION_FIELD02));
        transaction.setField03(fieldSet.readString(TRANSACTION_FIELD03));

        RegTypeOne regTypeOne = new RegTypeOne();
        regTypeOne.setRegId(fieldSet.readString(REG_TYPE_ONE_REG_ID));
        regTypeOne.setField01(fieldSet.readString(REG_TYPE_ONE_FIELD01));
        regTypeOne.setField02(fieldSet.readString(REG_TYPE_ONE_FIELD02));
        regTypeOne.setField03(fieldSet.readString(REG_TYPE_ONE_FIELD03));
        regTypeOne.setTransaction(transaction);
        regTypeOne.setId(transactionId);

        RegTypeTwo regTypeTwo = new RegTypeTwo();
        regTypeTwo.setRegId(fieldSet.readString(REG_TYPE_TWO_REG_ID));
        regTypeTwo.setField01(fieldSet.readString(REG_TYPE_TWO_FIELD01));
        regTypeTwo.setField02(fieldSet.readString(REG_TYPE_TWO_FIELD02));
        regTypeTwo.setField03(fieldSet.readString(REG_TYPE_TWO_FIELD03));
        regTypeTwo.setTransaction(transaction);
        regTypeTwo.setId(transactionId);

        RegTypeThree regTypeThree = new RegTypeThree();
        regTypeThree.setRegId(fieldSet.readString(REG_TYPE_THREE_REG_ID));
        regTypeThree.setField01(fieldSet.readString(REG_TYPE_THREE_FIELD01));
        regTypeThree.setField02(fieldSet.readString(REG_TYPE_THREE_FIELD02));
        regTypeThree.setField03(fieldSet.readString(REG_TYPE_THREE_FIELD03));
        regTypeThree.setTransaction(transaction);
        regTypeThree.setId(transactionId);

        transaction.setRegTypeOne(regTypeOne);
        transaction.setRegTypeTwo(regTypeTwo);
        transaction.setRegTypeThree(regTypeThree);

        return transaction;
    }

}
