package com.example.demo.models;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileHeader extends Register {
    public static final String LINE_ID = "0000";

    private String field01;
}
