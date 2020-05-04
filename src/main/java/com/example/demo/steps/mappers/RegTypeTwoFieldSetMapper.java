package com.example.demo.steps.mappers;

import com.example.demo.models.RegTypeTwo;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RegTypeTwoFieldSetMapper implements FieldSetMapper<RegTypeTwo> {
    public static final String FIELD21_COLUMN = "field21";
    public static final String FIELD22_COLUMN = "field22";
    public static final String FIELD23_COLUMN = "field23";
    public static final String FIELD24_COLUMN = "field24";
    public static final String FIELD25_COLUMN = "field25";

    @Override
    public RegTypeTwo mapFieldSet(FieldSet fieldSet) throws BindException {
        RegTypeTwo regTypeTwo = new RegTypeTwo();
        regTypeTwo.setField21(fieldSet.readString(FIELD21_COLUMN));
        regTypeTwo.setField22(fieldSet.readString(FIELD22_COLUMN));
        regTypeTwo.setField23(fieldSet.readString(FIELD22_COLUMN));
        regTypeTwo.setField24(fieldSet.readString(FIELD22_COLUMN));
        regTypeTwo.setField25(fieldSet.readString(FIELD22_COLUMN));
        return regTypeTwo;
    }

}
