package com.example.demo.steps.tokenizers;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.models.RegTypeOne;
import com.example.demo.models.RegTypeThree;
import com.example.demo.models.RegTypeTwo;
import com.example.demo.models.Transaction;

import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;

public class TransactionCompositeLineTokenizer extends PatternMatchingCompositeLineTokenizer {

    public TransactionCompositeLineTokenizer() {
        Map<String, LineTokenizer> tokenizers = new HashMap<>();
        tokenizers.put("*", defaulTokenizer());
        tokenizers.put(Transaction.LINE_ID + "*", transactionTokenizer());
        tokenizers.put(RegTypeOne.LINE_ID + "*", regTypeOneTokenizer());
        tokenizers.put(RegTypeTwo.LINE_ID + "*", regTypeTwoTokenizer());
        tokenizers.put(RegTypeThree.LINE_ID + "*", regTypeThreeTokenizer());
        this.setTokenizers(tokenizers);
    }

    @Bean
    public LineTokenizer defaulTokenizer() {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        String[] names = new String[] { "field01", "field02", "allColumns" };
        Range[] ranges = new Range[] { new Range(1, 2), new Range(3, 4), new Range(5, 99) };
        tokenizer.setNames(names);
        tokenizer.setColumns(ranges);
        tokenizer.setStrict(false);
        return tokenizer;
    }

    public LineTokenizer transactionTokenizer() {
        return new TransactionTokenizer();
    }

    private LineTokenizer regTypeOneTokenizer() {
        return new RegTypeOneTokenizer();
    }

    private LineTokenizer regTypeTwoTokenizer() {
        return new RegTypeTwoTokenizer();
    }

    private LineTokenizer regTypeThreeTokenizer() {
        return new RegTypeThreeTokenizer();
    }
}
