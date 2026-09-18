package com.example.demo.support;

import com.example.demo.models.RegTypeOne;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA usado APENAS pelos testes para verificar, sem depender de
 * navegação {@code LAZY} fora de sessão, que o sub-registro {@code 0101} de uma
 * transação foi persistido compartilhando a mesma chave primária (o id da
 * {@code Transaction}). Ver Property 1 (tarefa 8.2).
 */
public interface RegTypeOneTestRepository extends JpaRepository<RegTypeOne, Long> {
}
