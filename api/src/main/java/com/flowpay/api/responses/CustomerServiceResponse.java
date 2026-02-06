package com.flowpay.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowpay.api.entities.CustomerSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * DTO for {@link CustomerSession}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerServiceResponse implements Serializable {
    private Long id;
    private OffsetDateTime startedAt;
    private Integer queuePosition;

    public CustomerServiceResponse(CustomerSession customerSession, Integer queuePosition) {
        if (customerSession != null) {
            this.id = customerSession.getId();
            this.startedAt = customerSession.getStartedAt();
        }
        this.queuePosition = queuePosition;
    }

    public CustomerServiceResponse(CustomerSession customerSession) {
        this.id = customerSession.getId();
        this.startedAt = customerSession.getStartedAt();
    }
}