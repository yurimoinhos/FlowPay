package com.flowpay.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowpay.api.entities.Customer;
import com.flowpay.api.entities.CustomerSession;
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
public class CustomerResponse implements Serializable {
    private Long id;
    private String name;
    private String email;
    private CustomerServiceResponse currentService;


    public CustomerResponse(Customer customer, CustomerSession lastCustomerSession, Integer order) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.currentService = new CustomerServiceResponse(lastCustomerSession, order);
    }

    public CustomerResponse(Customer customer, CustomerSession service) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.currentService = new CustomerServiceResponse(service);
    }
}