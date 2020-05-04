package com.example.demo.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {
    public static final String LINE_ID = "0100";

    @Id
    @EqualsAndHashCode.Include
    private long id;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeOne regTypeOne;
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeTwo regTypeTwo;
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private RegTypeThree regTypeThree;

    private String field01;
    private String field02;
    private String field03;
    private String field04;
    private String field05;
}