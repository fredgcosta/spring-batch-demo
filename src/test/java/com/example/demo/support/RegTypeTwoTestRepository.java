package com.example.demo.support;

import com.example.demo.models.RegTypeTwo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA usado APENAS pelos testes para verificar que o sub-registro
 * {@code 0102} foi persistido compartilhando a chave primária da
 * {@code Transaction}. Ver Property 1 (tarefa 8.2).
 */
public interface RegTypeTwoTestRepository extends JpaRepository<RegTypeTwo, Long> {
}
