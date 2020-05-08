package com.example.demo.models;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileFooter extends Register {
    public static final String LINE_ID = "9999";

    private String field01;
}
