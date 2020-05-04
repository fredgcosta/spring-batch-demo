package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class TransactionTokenizer extends FixedLengthTokenizer {
    public TransactionTokenizer() {
        String[] names = new String[] {
                "field01",
                "field02",
                "id",
                "field03",
                "field04",
                "field05",
        };

        Range[] ranges = new Range[] {
                new Range(1, 2),
                new Range(3, 4),
                new Range(5, 22),
                new Range(23, 29),
                new Range(30, 36),
                new Range(37, 43)
        };
        this.setNames(names);
        this.setColumns(ranges);
        this.setStrict(false);
    }
}
