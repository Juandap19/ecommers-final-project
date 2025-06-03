package com.e2e.tests.payment;

import com.e2e.tests.util.E2ESuite;
import com.e2e.tests.util.TestRestFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentServiceE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    private Integer getOrderId() {
        return 1;
    }

    @Test
    void shouldSavePayment() {
        Integer orderId = getOrderId();
        Map<String, Object> orderReferencePayload = Map.of(
                "orderId", orderId
        );

        Map<String, Object> paymentPayload = Map.of(
                "isPayed", true,
                "paymentStatus", "COMPLETED",
                "order", orderReferencePayload
        );

        ResponseEntity<String> response = restFacade.post(
                paymentServiceUrl + "/payment-service/api/payments",
                paymentPayload,
                String.class);
        System.out.println("Response: " + response.getBody());
        System.out.println("Status Code: " + response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());

    }
}
