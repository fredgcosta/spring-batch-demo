package com.example.demo.steps.tokenizers;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;

public class TransactionTokenizer extends FixedLengthTokenizer {
    public TransactionTokenizer() {
        final String[] names = new String[] {
                "transactionRegId",
                "transactionId",
                "transactionField01",
                "transactionField02",
                "transactionField03",
                "regTypeOneRegId",
                "regTypeOneField01",
                "regTypeOneField02",
                "regTypeOneField03",
                "regTypeTwoRegId",
                "regTypeTwoField01",
                "regTypeTwoField02",
                "regTypeTwoField03",
                "regTypeThreeRegId",
                "regTypeThreeField01",
                "regTypeThreeField02",
                "regTypeThreeField03"
        };
        /*
         * File template
         * ----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5----0----5--
         * -
         * 
         * 0100000000000000000001campo03campo04campo050101campo13campo14campo150102campo23campo24campo250103campo33campo34campo35
         */
        final Range[] ranges = new Range[] {
                new Range(1, 4), //4
                new Range(5, 22), //18
                new Range(23, 29), //7
                new Range(30, 36), //7 
                new Range(37, 43), //7
                new Range(44, 47), //4
                new Range(48, 54), //7
                new Range(55, 61), //7
                new Range(62, 68), //7
                new Range(69, 72), //4
                new Range(73, 79), //7
                new Range(80, 86), //7
                new Range(87, 93), //7
                new Range(94, 97), //4
                new Range(98, 104), //7
                new Range(105, 111), //7
                new Range(112, 118), //7

        };
        this.setNames(names);
        this.setColumns(ranges);
        this.setStrict(false);
    }
}
