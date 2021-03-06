package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class FileFooterTokenizer extends FixedLengthTokenizer {
    public FileFooterTokenizer() {
        final String[] names = new String[] {
                "fileFooterRegId",
                "fileFooterField01"
        };
        /*
         * File template
         * ----5----0----5----0----5----0-
         * 
         * 0000este eh o header do arquivo
         */
        final Range[] ranges = new Range[] {
                new Range(1, 4), //4
                new Range(5, 31)
        };
        this.setNames(names);
        this.setColumns(ranges);
        this.setStrict(false);
    }
}
