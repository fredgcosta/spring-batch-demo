package com.example.demo.steps.mappers;

import com.example.demo.models.RegTypeOne;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RegTypeOneFieldSetMapper implements FieldSetMapper<RegTypeOne> {
    public static final String FIELD11_COLUMN = "field11";
    public static final String FIELD12_COLUMN = "field12";
    public static final String FIELD13_COLUMN = "field13";
    public static final String FIELD14_COLUMN = "field14";
    public static final String FIELD15_COLUMN = "field15";

    @Override
    public RegTypeOne mapFieldSet(FieldSet fieldSet) throws BindException {
        RegTypeOne regTypeOne = new RegTypeOne();
        regTypeOne.setField11(fieldSet.readString(FIELD11_COLUMN));
        regTypeOne.setField12(fieldSet.readString(FIELD12_COLUMN));
        regTypeOne.setField13(fieldSet.readString(FIELD12_COLUMN));
        regTypeOne.setField14(fieldSet.readString(FIELD12_COLUMN));
        regTypeOne.setField15(fieldSet.readString(FIELD12_COLUMN));
        return regTypeOne;
    }

}
