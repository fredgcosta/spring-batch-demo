package com.example.demo.models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Register {

    private String regId;

    @Id
    @EqualsAndHashCode.Include
    private long id;
}