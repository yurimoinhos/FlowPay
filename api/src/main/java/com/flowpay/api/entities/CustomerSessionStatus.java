package com.flowpay.api.entities;

/**
 * Statuses possíveis para uma sessão de atendimento ao cliente.
 * Podendo ser:
 * - Sessao aguardandoo atendimento -> PENDING
 * - Sessao em atendimento -> IN_PROGRESS
 * - Sessao finalizada -> COMPLETED
 * - Sessao cancelada pelo cliente ou atendente -> CANCELED
 */
public enum CustomerSessionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
}
