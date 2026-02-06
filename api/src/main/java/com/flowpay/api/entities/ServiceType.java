package com.flowpay.api.entities;


/**
 * Tipos de serviços oferecidos aos clientes.
 * Podendo ser:
 * - CARD_PROBLEMS: Problemas com cartao
 * - LOANS: Emprestimos
 * - OTHER: Outros serviços
 */
public enum ServiceType {
    CARD_PROBLEMS,
    LOANS,
    OTHER
}
