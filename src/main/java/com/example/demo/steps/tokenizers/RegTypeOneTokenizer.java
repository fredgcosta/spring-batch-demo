package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class RegTypeOneTokenizer extends FixedLengthTokenizer {
    public RegTypeOneTokenizer() {
        String[] names = new String[] {
                "field11",
                "field12",
                "field13",
                "field14",
                "field15",
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
