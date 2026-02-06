package com.flowpay.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowpay.api.entities.ServiceType;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link com.flowpay.api.entities.Customer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRequest implements Serializable {
    private String name;
    @Nonnull
    private String email;
    @Nonnull
    private ServiceType serviceType;
}