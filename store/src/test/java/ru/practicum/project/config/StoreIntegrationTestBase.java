package ru.practicum.project.config;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import ru.practicum.project.service.PaymentClient;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public abstract class StoreIntegrationTestBase extends PostgresR2dbcTestBase {

    @MockBean
    protected PaymentClient paymentClient;

    @DynamicPropertySource
    static void testProps(DynamicPropertyRegistry r) {
        r.add("spring.security.oauth2.client.registration.payment-client.client-id", () -> "store");
        r.add("spring.security.oauth2.client.registration.payment-client.client-secret", () -> "store-secret");
        r.add("spring.security.oauth2.client.registration.payment-client.authorization-grant-type", () -> "client_credentials");
        r.add("spring.security.oauth2.client.registration.payment-client.scope", () -> "payment.write");
        r.add("spring.security.oauth2.client.registration.payment-client.provider", () -> "payment-provider");
        r.add("spring.security.oauth2.client.provider.payment-provider.token-uri", () -> "http://localhost:9999/oauth2/token");
        
        r.add("spring.data.redis.host", () -> "localhost");
        r.add("spring.data.redis.port", () -> 6379);
        r.add("spring.cache.type", () -> "NONE");
        
        r.add("payments.base-url", () -> "http://localhost:9999");
        r.add("spring.session.store-type", () -> "none");
        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://issuer.test");
    }
}

