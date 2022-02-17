package com.infoledger.enclave.service.host;

import com.infoledger.enclave.service.host.configuration.InfoLedgerEnlaveServiceHostWebConfiguration;
import com.infoledger.enclave.service.host.configuration.S3ClientConfiguration;
import com.infoledger.enclave.service.host.configuration.SecurityConfiguration;
import com.infoledger.enclave.service.host.configuration.SwaggerConfiguration;
import com.infoledger.enclave.service.host.configuration.cognito.CognitoConfiguration;
import com.infoledger.enclave.service.host.configuration.embedded.EmbeddedS3ClientConfiguration;
import com.infoledger.enclave.service.host.configuration.factory.YamlPropertySourceFactory;
import com.infoledger.enclave.service.host.configuration.logging.decorator.MdcTaskDecorator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * InfoLedger enclave service application
 */
@SpringBootApplication
@Import(value = {
        S3ClientConfiguration.class,
        EmbeddedS3ClientConfiguration.class,
        CognitoConfiguration.class,
        SecurityConfiguration.class,
        InfoLedgerEnlaveServiceHostWebConfiguration.class,
        SwaggerConfiguration.class})
@EnableAsync(proxyTargetClass = true)
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class InfoLedgerEnclaveServiceHostApplication extends AsyncConfigurerSupport {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * Ctor.
     */
    protected InfoLedgerEnclaveServiceHostApplication() {
    }

    /**
     * Run proxy service application
     *
     * @param args some params
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(InfoLedgerEnclaveServiceHostApplication.class)
                .logStartupInfo(true)
                .run(args);
    }
}
