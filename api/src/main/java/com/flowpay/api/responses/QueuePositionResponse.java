package com.flowpay.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowpay.api.entities.CustomerSessionStatus;
import com.flowpay.api.entities.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueuePositionResponse implements Serializable {
    private Long position;
    private CustomerSessionStatus status;
    private ServiceType serviceType;
    private OffsetDateTime timestamp;
}
