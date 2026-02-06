package com.flowpay.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMetricsResponse implements Serializable {
    private Double averageSessionsPerCustomer;
    private Double averageServiceDurationSeconds;
    private Long pendingCount;
    private Long inProgressCount;
    private Long completedCount;
    private Long canceledCount;
    private Long totalSessions;
    private Double completionRate;
    private Double cancellationRate;
}
