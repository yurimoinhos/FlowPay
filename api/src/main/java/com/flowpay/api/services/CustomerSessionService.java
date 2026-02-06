package com.flowpay.api.services;

import com.flowpay.api.entities.Customer;
import com.flowpay.api.entities.CustomerSession;
import com.flowpay.api.entities.CustomerSessionStatus;
import com.flowpay.api.entities.ServiceType;
import com.flowpay.api.repositories.CustomerRepository;
import com.flowpay.api.repositories.CustomerSessionRepository;
import com.flowpay.api.requests.CustomerRequest;
import com.flowpay.api.responses.QueuePositionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Slf4j
@org.springframework.stereotype.Service
public class CustomerSessionService {

    private static final int MAX_SLOTS_PER_SERVICE = 3;
    
    private final CustomerRepository customerRepository;
    private final CustomerSessionRepository customerSessionRepository;

    /**
     * Busca a posiçao na fila do cliente pelo email.
     * 
     * @param email - Email do cliente
     * @return
     */
    public Mono<Long> fetchOrderedCustomerSession(String email) {
        return customerSessionRepository.findQueuePositionByCustomerEmail(email);
    }

    /**
     * Cria um novo cliente e inicia uma sessao de atendimento.
     * Caso o cliente já exista, apenas uma nova sessao é criada.
     * 
     * @throws IllegalArgumentException se o tipo de serviço nao for fornecido
     * @throws IllegalArgumentException se o cliente já possuir uma sessao ativa
     * @param customerRequest - DTO de Request com dados de um cliente 
     *                          e o serviço desejado para criaçao da sessao
     * @return Mono<Void>
     */
    public Mono<Void> createCustomer(CustomerRequest customerRequest) {
        String customerEmail = customerRequest.getEmail();
        ServiceType serviceType = customerRequest.getServiceType();

        if (serviceType == null) {
            return Mono.error(new IllegalArgumentException("Tipo de serviço é obrigatório"));
        }

        return customerRepository.existsByEmail(customerEmail)
            .flatMap(exists -> fetchCustomerOrCreate(customerRequest, exists))
            .flatMap(customer -> 
                customerSessionRepository.findByActiveServicesByCustomerEmail(customerEmail)
                    .flatMap(existingSession -> {
                        log.warn("Cliente {} já possui sessao ativa", customerEmail);
                        return Mono.error(new IllegalArgumentException("Sessao ativa já existe para este cliente"));
                    })
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            CustomerSession session = new CustomerSession(customer.getId(), serviceType);
                            return customerSessionRepository.save(session)
                                .doOnNext(savedSession -> 
                                    log.info("sessao criada: ID={} cliente={} tipo={}", 
                                        savedSession.getId(), customerEmail, serviceType))
                                .then(tryPromoteNextInQueue(serviceType));
                        })
                    )
            ).then();
    }

    /**
     * Método interno que realiza upsert do cliente (Criando caso nao exissta ou buscando o existente).
     * @param customerRequest
     * @param exists
     * @return Mono<Customer>
     */
    private Mono<Customer> fetchCustomerOrCreate(CustomerRequest customerRequest, Boolean exists) {
        if (exists) {
            return customerRepository.findByEmail(customerRequest.getEmail());
        } else {
            Customer newCustomer = new Customer(
                customerRequest.getName(),
                customerRequest.getEmail()
            );
            return customerRepository.save(newCustomer);
        }
    }

    /**
     * Finaliza a sessao do cliente com o status apropriado.
     * Caso o cliente esteja em atendimento, a sessao é finalizada informando
     * o atendimento como completo (COMPLETED).
     * Caso o cliente esteja apenas na fila de espera, a sessao é finalizada
     * como cancelada (CANCELED).
     * 
     * @throws IllegalArgumentException se o cliente nao for encontrado ou nenhuma sessao ativa for encontrada.
     * @param email
     * @return Mono<Void>
     */
    public Mono<Void> finishCustomerSession(String email) {
        return customerRepository.existsByEmail(email)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new IllegalArgumentException("Usuário nao encontrado"));
                }
                
                return customerSessionRepository.findByActiveServicesByCustomerEmail(email)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Nenhuma sessao ativa")))
                    .flatMap(session -> {
                        if (session.getFinishedAt() != null) {
                            return Mono.error(new IllegalArgumentException("sessao já finalizada"));
                        }
                        var currentStatus = session.getStatus();

                        CustomerSessionStatus newStatus = getNewStatus(currentStatus);
                        ServiceType serviceType = session.getServiceType();
                        log.info("Finalizando sessao para {} tipo {}", email,  serviceType);
                        return customerSessionRepository.setFinishedAtToNow(email, newStatus)
                            .then(tryPromoteNextInQueue(serviceType));
                    });
            });
    }

    /**
     * Verifica quantos slots estao disponíveis para um tipo de serviço.
     * @param serviceType
     * @return Mono<Integer>
     */
    public Mono<Integer> checkAvailableSlots(ServiceType serviceType) {
        return customerSessionRepository.countInProgressByServiceType(serviceType.name())
            .map(count -> MAX_SLOTS_PER_SERVICE - count.intValue());
    }

    /**
     * Tenta promover o próximo cliente na fila para IN_PROGRESS, se houver slots disponíveis
     * para o atendentimento do tipo de serviço. 
     * O processo é repetido até que nao haja mais slots ou clientes na fila.
     * @param serviceType - Tipo de serviço
     * @return Mono<Void>
     */
    private Mono<Void> tryPromoteNextInQueue(ServiceType serviceType) {
        return checkAvailableSlots(serviceType)
            .flatMap(availableSlots -> {
                if (availableSlots <= 0) {
                    return Mono.empty();
                }
                
                log.info("Promovendo até {} clientes para o serviço {}", availableSlots, serviceType);
                
                return customerSessionRepository.findNextInQueueByServiceType(serviceType.name())
                    .repeat(availableSlots - 1)
                    .flatMap(session -> {
                        session.setStatus(CustomerSessionStatus.IN_PROGRESS);
                        return customerSessionRepository.save(session)
                            .doOnNext(s -> log.info("sessao {} promovida para IN_PROGRESS", s.getId()));
                    })
                    .then();
            })
            .then();
    }

    /**
     * A posiçao da fila do cliente é atualizada a cada segundo e enviada via SSE.
     * 
     * Caso o fluxo for encerrado repentinamente antes do inicio do atendimento,
     * a sessao do cliente é finalizada com status CANCELED, cancelando seu atendimento e
     * precisando iniciar outro.
     * 
     * Caso o fluxo for encerrado durante o atendimento, a sessao do cliente nao é afetada,
     * podendo ele retornar posteriormente para finalizar o atendimento.
     * 
     * Nesse momento, caso necessário o atendente pode finalizar a sessao manualmente
     * através do método de finalizaçao de sessao.
     * 
     * @see finishCustomerSession
     * @param email - O email do cliente atual
     * @return Flux - Fluxo de atualizaçoes da atual posiçaoo do usuário na fila
     */
    public Flux<QueuePositionResponse> streamQueueUpdates(String email) {
        log.info("Iniciando stream SSE para {}", email);
        
        Mono<QueuePositionResponse> initialUpdate = customerSessionRepository
            .findByActiveServicesByCustomerEmail(email)
            .flatMap(session ->
                customerSessionRepository.findQueuePositionByCustomerEmail(email)
                    .map(position -> new QueuePositionResponse(
                        position,
                        session.getStatus(),
                        session.getServiceType(),
                        OffsetDateTime.now()
                    ))
            );
        
        Flux<QueuePositionResponse> pollingUpdates = Flux.interval(Duration.ofSeconds(1))
            .flatMap(tick -> 
                customerSessionRepository.findByActiveServicesByCustomerEmail(email)
                    .flatMap(session ->
                        customerSessionRepository.findQueuePositionByCustomerEmail(email)
                            .map(position -> new QueuePositionResponse(
                                position,
                                session.getStatus(),
                                session.getServiceType(),
                                OffsetDateTime.now()
                            ))
                    )
            )
            .takeUntil(update -> update.getStatus() == CustomerSessionStatus.COMPLETED);
        
        return initialUpdate.flux()
            .concatWith(pollingUpdates)
            .distinctUntilChanged(update -> update.getPosition() + "|" + update.getStatus())
            .doOnNext(update -> log.info("SSE enviado para {}: posiçao={} status={}", 
                email, update.getPosition(), update.getStatus()))
            .timeout(Duration.ofHours(1))
            .doOnCancel(() -> {
                log.info("Stream cancelado para {}, finalizando sessao...", email);
                finishCustomerSession(email).ignoreElement();
            })
            .doOnComplete(() -> {
                log.info("Stream completado para {}, finalizando sessao...", email);
                finishCustomerSession(email).ignoreElement();
            });
        }

        private CustomerSessionStatus getNewStatus(CustomerSessionStatus currentStatus) {
            return switch (currentStatus) {
                case IN_PROGRESS -> CustomerSessionStatus.COMPLETED;
                case PENDING -> CustomerSessionStatus.CANCELED;
                default -> currentStatus;
            };
        }
}
