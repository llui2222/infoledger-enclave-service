package com.infoledger.enclave.service.host.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoledger.aggregation.enclave.client.EnclaveClient;
import com.infoledger.enclave.service.host.configuration.factory.YamlPropertySourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static java.util.Objects.requireNonNull;

/**
 * InfoLedger Aggregation Service application configuration
 */
@Configuration
@ComponentScan(
        basePackages = {
                "com.infoledger.enclave.service.host"
        }
)
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class InfoLedgerEnlaveServiceHostWebConfiguration implements WebMvcConfigurer {

    @Bean
    public EnclaveClient enclaveClient() {
        String enclaveCid = System.getProperty("enclave.cid");
        String enclavePort = System.getProperty("enclave.port");

        requireNonNull(enclaveCid, "Enclave cid must be provided.");
        requireNonNull(enclavePort, "Enclave port must be provided.");

        return new EnclaveClient(Integer.parseInt(enclaveCid),
                Integer.parseInt(enclavePort));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);

        return mapper;
    }
}
