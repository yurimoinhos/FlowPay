package com.flowpay.api.controllers;

import com.flowpay.api.repositories.CustomerSessionRepository;
import com.flowpay.api.requests.CustomerRequest;
import com.flowpay.api.responses.QueuePositionResponse;
import com.flowpay.api.services.CustomerSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerSessionController {

    private final CustomerSessionService customerService;
    private final CustomerSessionRepository customerSessionRepository;

    /**
     * Fornece um stream SSE com atualizações da posição na fila do cliente usnado o email como identificador.
     * O stream permanece ativo até que a sessão seja finalizada ou ocorra timeout de 1 hora.
     * 
     * @param email - Email do cliente
     * @return QueuePositionResponse - DTO de Response com a posiçao atual na fila e o tempo estimado de espera
     * @throws IllegalArgumentException se nenhuma sessao ativa for encontrada para o email fornecido
     * @throws IllegalStateException se a sessao ativa for encontrada mas nao estiver mais na fila (PENDING)
     */
    @GetMapping(value = "/{email}/queue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<QueuePositionResponse>> getQueuePositionStream(@PathVariable String email) {
        return customerService.streamQueueUpdates(email)
            .map(update -> ServerSentEvent.<QueuePositionResponse>builder()
                .event("queue-update")
                .data(update)
                .build())
            .doOnSubscribe(sub -> log.info("Cliente {} conectado ao stream de fila", email))
            .doOnCancel(() -> customerService.finishCustomerSession(email))
            .doOnError(err -> log.error("Erro no stream de fila para {}: {}", email, err.getMessage()));
    }

    /**
     * Cria um novo cliente e inicia uma sessao de atendimento.
     * Caso o cliente já exista, apenas uma nova sessão é criada.
     * 
     * 
     *
     * @throws IllegalArgumentException se o tipo de serviço não for fornecido
     * @throws IllegalArgumentException se o cliente já possuir uma sessão ativa
     * @param customerRequest - DTO de Request com dados de um cliente
     *                          e o serviço desejado para criação da sessão
     * @return Mono<Void>
     */
    @PostMapping
    public Mono<ResponseEntity<Void>> createCustomer(@RequestBody CustomerRequest customerRequest) {
            return customerService.createCustomer(customerRequest)
            .thenReturn(ResponseEntity.status(303).location(URI.create("/api/customer/" + customerRequest.getEmail() + "/queue")).build());
    }

    /**
     * Finaliza a sessão de atendimento ativa do cliente identificado pelo email
     * 
     * @throws IllegalArgumentException se o cliente nao for encontrado ou nenhuma sessao ativa for encontrada
     * @param email
     * @return Mono<Void>
     */
    @PutMapping("/{email}")
    public Mono<Void> finishCustomerSession(@PathVariable String email) {
        return customerService.finishCustomerSession(email);
    }

    /**
     * Verifica quantos slots estao disponíveis para o tipo de serviço
     * da sessao ativa do cliente identificado pelo email.
     * 
     * @throws IllegalArgumentException se nenhuma sessao ativa for encontrada
     * @param email
     * @return Mono<Integer>
     */
    @GetMapping("/{email}/slots-available")
    public Mono<Integer> getSlotsAvailable(@PathVariable String email) {
        return customerSessionRepository.findByActiveServicesByCustomerEmail(email)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Nenhuma sessao ativa encontrada")))
            .flatMap(session -> customerService.checkAvailableSlots(session.getServiceType()));
    }
}
