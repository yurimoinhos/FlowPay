package com.flowpay.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;


/**
 * Aplicação para entrevista tecnica da UBots.
 * O sistema simula um atendimento ao cliente 
 * com filas de espera e gerenciamento de sessoes 
 * via API REST usando notificacoes em tempo real via SSE.
 */
@SpringBootApplication
@EnableR2dbcRepositories
public class FlowPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowPayApplication.class, args);
    }

}
