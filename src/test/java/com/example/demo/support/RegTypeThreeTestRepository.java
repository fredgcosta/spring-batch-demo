package com.example.demo.support;

import com.example.demo.models.RegTypeThree;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA usado APENAS pelos testes para verificar que o sub-registro
 * {@code 0103} foi persistido compartilhando a chave primária da
 * {@code Transaction}. Ver Property 1 (tarefa 8.2).
 */
public interface RegTypeThreeTestRepository extends JpaRepository<RegTypeThree, Long> {
}
