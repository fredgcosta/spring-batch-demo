package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class DefaultTokenizer extends FixedLengthTokenizer {
    public DefaultTokenizer() {
        String[] names = new String[] {
                "regId"
        };

        Range[] ranges = new Range[] {
                new Range(1, 4)
        };
        this.setNames(names);
        this.setColumns(ranges);
        this.setStrict(false);
    }
}