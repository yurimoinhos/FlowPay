package com.flowpay.api.config;

import com.flowpay.api.entities.CustomerSessionStatus;
import com.flowpay.api.entities.ServiceType;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfiguration(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        
        converters.add(new ServiceReadingConverter());
        converters.add(new ServiceWritingConverter());
        
        converters.add(new CustomerServiceStatusReadingConverter());
        converters.add(new CustomerServiceStatusWritingConverter());
        
        converters.add(new LocalDateTimeToOffsetDateTimeConverter());
        converters.add(new OffsetDateTimeToLocalDateTimeConverter());
        
        return new R2dbcCustomConversions(getStoreConversions(), converters);
    }

    // Service converters
    @ReadingConverter
    static class ServiceReadingConverter implements Converter<String, ServiceType> {
        @Override
        public ServiceType convert(String source) {
            return ServiceType.valueOf(source);
        }
    }

    @WritingConverter
    static class ServiceWritingConverter implements Converter<ServiceType, String> {
        @Override
        public String convert(ServiceType source) {
            return source.name();
        }
    }

    // CustomerServiceStatus converters
    @ReadingConverter
    static class CustomerServiceStatusReadingConverter implements Converter<String, CustomerSessionStatus> {
        @Override
        public CustomerSessionStatus convert(String source) {
            return CustomerSessionStatus.valueOf(source);
        }
    }

    @WritingConverter
    static class CustomerServiceStatusWritingConverter implements Converter<CustomerSessionStatus, String> {
        @Override
        public String convert(CustomerSessionStatus source) {
            return source.name();
        }
    }

    // MessageSenderType converters
    @ReadingConverter
    static class LocalDateTimeToOffsetDateTimeConverter implements Converter<LocalDateTime, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(LocalDateTime source) {
            return source.atOffset(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    static class OffsetDateTimeToLocalDateTimeConverter implements Converter<OffsetDateTime, LocalDateTime> {
        @Override
        public LocalDateTime convert(OffsetDateTime source) {
            return source.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
    }
}
