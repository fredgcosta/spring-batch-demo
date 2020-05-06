package com.example.demo.steps.mappers;

import com.example.demo.models.FileHeader;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class FileHeaderFieldSetMapper implements FieldSetMapper<FileHeader> {
    public static final String FILE_HEADER_REG_ID = "fileHeaderRegId";
    public static final String FILE_HEADER_FIELD01 = "fileHeaderField01";

    @Override
    public FileHeader mapFieldSet(FieldSet fieldSet) throws BindException {
        FileHeader fileHeader = new FileHeader();
        fileHeader.setRegId(fieldSet.readString(FILE_HEADER_REG_ID));
        fileHeader.setField01(fieldSet.readString(FILE_HEADER_FIELD01));

        return fileHeader;
    }

}
