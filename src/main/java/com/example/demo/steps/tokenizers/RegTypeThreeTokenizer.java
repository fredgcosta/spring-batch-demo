package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class RegTypeThreeTokenizer extends FixedLengthTokenizer {
    public RegTypeThreeTokenizer() {
        String[] names = new String[] {
                "field31",
                "field32",
                "field33",
                "field34",
                "field35",
        };

        Range[] ranges = new Range[] {
                new Range(1, 2),
                new Range(3, 4),
                new Range(5, 11),
                new Range(12, 18),
                new Range(19, 25)
        };
        this.setNames(names);
        this.setColumns(ranges);
        this.setStrict(false);
    }
}
