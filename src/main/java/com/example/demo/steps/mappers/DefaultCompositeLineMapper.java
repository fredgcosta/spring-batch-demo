package com.example.demo.steps.mappers;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.models.Transaction;
import com.example.demo.steps.tokenizers.DefaultTokenizer;
import com.example.demo.steps.tokenizers.TransactionTokenizer;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class DefaultCompositeLineMapper extends PatternMatchingCompositeLineMapper<Transaction> {
    public DefaultCompositeLineMapper() {
        Map<String, LineTokenizer> tokenizers = new HashMap<>();
        tokenizers.put(Transaction.LINE_ID + "*", new TransactionTokenizer());
        //TODO Consertar o tipo Generics
        // tokenizers.put(FileHeader.LINE_ID + "*", new FileHeaderTokenizer());
        // tokenizers.put(FileFooter.LINE_ID + "*", new FileFooterTokenizer());
        tokenizers.put("*", new DefaultTokenizer());
        this.setTokenizers(tokenizers);

        Map<String, FieldSetMapper<Transaction>> fieldSetMappers = new HashMap<>();
        fieldSetMappers.put(Transaction.LINE_ID + "*", new TransactionFieldSetMapper());
        // fieldSetMappers.put(FileHeader.LINE_ID + "*", new FileHeaderFieldSetMapper());
        // fieldSetMappers.put(FileFooter.LINE_ID + "*", new FileFooterFieldSetMapper());
        fieldSetMappers.put("*", new DefaultFieldSetMapper());
        this.setFieldSetMappers(fieldSetMappers);
    }
}