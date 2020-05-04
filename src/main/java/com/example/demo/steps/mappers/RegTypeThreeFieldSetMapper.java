package com.example.demo.steps.mappers;

import com.example.demo.models.RegTypeThree;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RegTypeThreeFieldSetMapper implements FieldSetMapper<RegTypeThree> {
    public static final String FIELD31_COLUMN = "field31";
    public static final String FIELD32_COLUMN = "field32";
    public static final String FIELD33_COLUMN = "field33";
    public static final String FIELD34_COLUMN = "field34";
    public static final String FIELD35_COLUMN = "field35";

    @Override
    public RegTypeThree mapFieldSet(FieldSet fieldSet) throws BindException {
        RegTypeThree regTypeThree = new RegTypeThree();
        regTypeThree.setField31(fieldSet.readString(FIELD31_COLUMN));
        regTypeThree.setField32(fieldSet.readString(FIELD32_COLUMN));
        regTypeThree.setField33(fieldSet.readString(FIELD32_COLUMN));
        regTypeThree.setField34(fieldSet.readString(FIELD32_COLUMN));
        regTypeThree.setField35(fieldSet.readString(FIELD32_COLUMN));
        return regTypeThree;
    }

}
