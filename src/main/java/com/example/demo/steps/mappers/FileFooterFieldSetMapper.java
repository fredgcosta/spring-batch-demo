package com.example.demo.steps.mappers;

import com.example.demo.models.FileFooter;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class FileFooterFieldSetMapper implements FieldSetMapper<FileFooter> {
    public static final String FILE_FOOTER_REG_ID = "fileFooterRegId";
    public static final String FILE_FOOTER_FIELD01 = "fileFooterField01";

    @Override
    public FileFooter mapFieldSet(FieldSet fieldSet) throws BindException {
        FileFooter fileFooter = new FileFooter();
        fileFooter.setRegId(fieldSet.readString(FILE_FOOTER_REG_ID));
        fileFooter.setField01(fieldSet.readString(FILE_FOOTER_FIELD01));

        return fileFooter;

    }

}
