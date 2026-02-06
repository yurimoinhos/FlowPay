package com.flowpay.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
class GlobalControllerAdvice {

    /**
     * Trata exceçoes que herdam de HttpStatusCodeException
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleHttpStatusCodeException(HttpStatusCodeException ex) {
        log.error("HttpStatusCodeException: {} - {}", ex.getStatusCode(), ex.getStatusText(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getStatusText()
        );
        problemDetail.setTitle(ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(problemDetail));
    }

    /**
     * Trata exceçoes de validaçao de argumentos (@Valid) em WebFlux
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Erro de validaçao: {}", ex.getMessage());
        log.debug("Stacktrace de validaçao:", ex);
        
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "A validaçao falhou para um ou mais campos"
        );
        problemDetail.setTitle("Erro de Validaçao");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("fieldErrors", fieldErrors);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    /**
     * Trata exceçoes de input inválido no WebFlux
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Entrada inválida: {}", ex.getReason(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getReason() != null ? ex.getReason() : "Entrada inválida"
        );
        problemDetail.setTitle("Parâmetro Inválido");
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    /**
     * Trata exceçoes de integridade de dados (ex: chave duplicada, FK violation)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Violaçao de integridade de dados: {}", ex.getMessage(), ex);
        
        String message = "Violaçao de integridade de dados.";
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("duplicate key")) {
                message = "Registro duplicado. Este dado já existe no sistema.";
                log.warn("Tentativa de inserir chave duplicada");
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Violaçao de chave estrangeira. Registro relacionado nao existe.";
                log.warn("Tentativa de violaçao de foreign key");
            } else if (ex.getMessage().contains("not-null")) {
                message = "Campo obrigatório nao foi preenchido.";
                log.warn("Tentativa de inserir valor nulo em campo obrigatório");
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                message
        );
        problemDetail.setTitle("Conflito de Dados");
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail));
    }

    /**
     * Trata exceçoes de argumento ilegal
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Argumento inválido fornecido"
        );
        problemDetail.setTitle("Argumento Inválido");
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    /**
     * Trata exceçoes de estado ilegal
     */
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Estado ilegal detectado: {}", ex.getMessage(), ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage() != null ? ex.getMessage() : "Operaçao nao permitida no estado atual"
        );
        problemDetail.setTitle("Estado Inválido");
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail));
    }

    /**
     * Trata exceçoes de null pointer
     */
    @ExceptionHandler(NullPointerException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException detectado - possível bug no código!", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno: referência nula encontrada"
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setProperty("timestamp", Instant.now());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }

    /**
     * Handler genérico para todas as outras exceçoes nao tratadas
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGenericException(Exception ex) {
        log.error("Exceçao nao tratada capturada: {} - {}", 
                ex.getClass().getName(), 
                ex.getMessage(), 
                ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }
}
