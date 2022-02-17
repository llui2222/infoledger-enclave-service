package com.infoledger.enclave.service.host;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link InfoLedgerEnclaveServiceHostApplication} verifying that context can be loaded.
 */
@SpringBootTest
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
@ActiveProfiles({"local", "test"})
@DirtiesContext
class InfoLedgerEnclaveServiceHostApplicationUnitTest {

    @Autowired
    private WebApplicationContext webAppContext;

    @Test
    void contextLoads() {
        // verifies that context loads
        assertThat(webAppContext).isNotNull();
    }
}
