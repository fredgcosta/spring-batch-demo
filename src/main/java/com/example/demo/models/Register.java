package com.example.demo.models;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

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