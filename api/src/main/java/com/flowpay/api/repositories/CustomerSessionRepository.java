package com.flowpay.api.repositories;

import com.flowpay.api.entities.CustomerSessionStatus;
import com.flowpay.api.entities.CustomerSession;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerSessionRepository extends ReactiveCrudRepository<CustomerSession, Long> {

    /**
     * Calcula a posiçao na fila para um atendimento específico pelo email.
     * Considera apenas sessoes do mesmo tipo de serviço.
     *
     * @param email Email do cliente
     * @return Posiçao na fila (1 = primeiro da fila)
     */
    @Query("""
           SELECT COUNT(*) + 1
           FROM customer_sessions cs
           WHERE cs.started_at < (
               SELECT s.started_at
               FROM customer_sessions s
               JOIN customers c ON s.customer_id = c.id
               WHERE c.email = :email
               AND s.finished_at IS NULL
               ORDER BY s.started_at DESC
               LIMIT 1
           )
           AND cs.finished_at IS NULL
           AND cs.status = 'PENDING'
           AND cs.service_type = (
               SELECT s.service_type
               FROM customer_sessions s
               JOIN customers c ON s.customer_id = c.id
               WHERE c.email = :email
               AND s.finished_at IS NULL
               ORDER BY s.started_at DESC
               LIMIT 1
           )
           """)
    Mono<Long> findQueuePositionByCustomerEmail(String email);

    /**
     * Retorna o próximo atendimento a ser chamado (primeiro da fila FIFO) de um tipo de serviço.
     *
     * @param serviceType Tipo de serviço
     * @return Primeiro atendimento da fila
     */
    @Query("""
       SELECT * FROM customer_sessions
       WHERE finished_at IS NULL
       AND status = 'PENDING'
       AND service_type = :serviceType
       ORDER BY started_at
       LIMIT 1
       """)
    Mono<CustomerSession> findNextInQueueByServiceType(String serviceType);

    /**
     * Retorna o próximo atendimento a ser chamado (primeiro da fila FIFO).
     *
     * @return Primeiro atendimento da fila
     */
    @Query("SELECT * FROM customer_sessions " +
           "WHERE finished_at IS NULL " +
           "AND status = 'PENDING' " +
           "ORDER BY started_at " +
           "LIMIT 1")
    Mono<CustomerSession> findNextInQueue();

    /**
     * Retorna todos os atendimentos em andamento (IN_PROGRESS).
     *
     * @return Lista de atendimentos em andamento
     */
    @Query("SELECT * FROM customer_sessions " +
           "WHERE finished_at IS NULL " +
           "AND status = 'IN_PROGRESS' " +
           "ORDER BY started_at")
    Flux<CustomerSession> findAllInProgress();


    @Query("""
            UPDATE customer_sessions
            SET finished_at = CURRENT_TIMESTAMP, status = :status
            WHERE customer_id = (SELECT id FROM customers WHERE email = :email)
    """)
    Mono<Void> setFinishedAtToNow(String email, CustomerSessionStatus status);


    /**
     * Conta quantos atendimentos estao na fila aguardando.
     *
     * @return Total de atendimentos na fila
     */
    @Query("""
           SELECT COUNT(*) FROM customer_sessions
           WHERE finished_at IS NULL
           AND status = 'PENDING'
          """)
    Mono<Long> countPendingInQueue();

    /**
     * Conta quantos atendimentos estao IN_PROGRESS para um tipo de serviço.
     *
     * @param serviceType Tipo de serviço
     * @return Total de atendimentos em progresso
     */
    @Query("""
    SELECT COUNT(*) FROM customer_sessions
    WHERE finished_at IS NULL
    AND status = 'IN_PROGRESS'
    AND service_type = :serviceType
    """)
    Mono<Long> countInProgressByServiceType(String serviceType);


    /**
     * Retorna todas as sessoes PENDING de um tipo de serviço.
     *
     * @param serviceType Tipo de serviço
     * @return Lista de sessoes pendentes
     */
    @Query("""
    SELECT * FROM customer_sessions
    WHERE finished_at IS NULL
    AND status = 'PENDING'
    AND service_type = :serviceType
    ORDER BY started_at
    """)
    Flux<CustomerSession> findAllPendingByServiceType(String serviceType);

    /**
     * Retorna todas as sessoes ativas (PENDING ou IN_PROGRESS) de um tipo de serviço.
     *
     * @param serviceType Tipo de serviço
     * @return Lista de sessoes ativas
     */
    @Query("""
    SELECT * FROM customer_sessions
    WHERE finished_at IS NULL
    AND (status = 'PENDING' OR status = 'IN_PROGRESS')
    AND service_type = :serviceType
    ORDER BY started_at
    """)
    Flux<CustomerSession> findAllActiveByServiceType(String serviceType);

    /**
     * Encontra a sessao ativa (sem finished_at) de um cliente pelo email.
     */
    @Query("""
    SELECT cs.*
    FROM customer_sessions cs
    JOIN customers c ON cs.customer_id = c.id
    WHERE c.email = :email
    AND cs.finished_at IS NULL
    """)
    Mono<CustomerSession> findByActiveServicesByCustomerEmail(String email);

}
