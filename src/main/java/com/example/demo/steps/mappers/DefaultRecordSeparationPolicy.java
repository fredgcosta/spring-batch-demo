package com.example.demo.steps.mappers;

import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;

public class DefaultRecordSeparationPolicy implements RecordSeparatorPolicy {
    private boolean isFirstRead = true;
    private String currentRecord = "";
    private String nextRecord = "";
    private boolean isHeader;

    @Override
    public boolean isEndOfRecord(String record) {
        if (record.startsWith("0000")) {
            isHeader = true;
            return true;
        } else if (record.startsWith("9999")) {
            return true;
        } else if (record.startsWith("0100")) {
            isHeader = false;
            if (isFirstRead) {
                isFirstRead = false;
                return false;
            } else {
                return true;
            }
        } else {
            isHeader = false;
            return false;
        }
    }

    /**
     * Pass the record through. Do nothing.
     * 
     * @see org.springframework.batch.item.file.separator.RecordSeparatorPolicy#postProcess(java.lang.String)
     */
    @Override
    public String postProcess(String record) {
        if (isHeader) {
            return record;
        }

        nextRecord = currentRecord;
        currentRecord = record;

        return nextRecord;

    }

    /**
     * Pass the line through. Do nothing.
     * 
     * @see org.springframework.batch.item.file.separator.RecordSeparatorPolicy#preProcess(java.lang.String)
     */
    @Override
    public String preProcess(String record) {
        currentRecord += record;
        record = "";
        return record;
    }

}