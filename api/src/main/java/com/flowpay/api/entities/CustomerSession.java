package com.flowpay.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("customer_sessions")
public class CustomerSession {

    @Id
    private Long id;

    @Column("customer_id")
    private Long customerId;

    @Column("service_type")
    private ServiceType serviceType;

    @Column("status")
    private CustomerSessionStatus status;

    @Column("started_at")
    private OffsetDateTime startedAt;

    @Column("finished_at")
    private OffsetDateTime finishedAt;

    @Version
    private Long version;

    public CustomerSession(Long customerId, ServiceType serviceType) {
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.status = CustomerSessionStatus.PENDING;
        this.startedAt = OffsetDateTime.now();
    }
}
