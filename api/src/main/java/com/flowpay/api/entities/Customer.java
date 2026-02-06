package com.flowpay.api.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Setter
@Getter
@Table("customers")
public class Customer {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

    @Column("created_at")
    private OffsetDateTime createdAt;

    public Customer() {
        this.createdAt = OffsetDateTime.now();
    }

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.createdAt = OffsetDateTime.now();
    }

}
