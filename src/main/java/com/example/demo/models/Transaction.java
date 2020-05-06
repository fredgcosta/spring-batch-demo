package com.example.demo.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction extends Register {
    public static final String LINE_ID = "0100";

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeOne regTypeOne;
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeTwo regTypeTwo;
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeThree regTypeThree;

    private String field01;
    private String field02;
    private String field03;
}