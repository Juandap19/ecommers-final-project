package com.e2e.tests.order;

import com.e2e.tests.util.E2ESuite;
import com.e2e.tests.util.TestRestFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderServiceE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade; // Autowire your facade

    @Value("${order.service.url}") // This property is set by E2ESuite.Initializer
    private String productServiceUrl;

    @Test
    void shouldGetOrderById() {
        int orderId = 2;
        ResponseEntity<String> response = restFacade.get(
                productServiceUrl + "/order-service/api/orders/" + orderId,
                String.class);
        System.out.println("Response: " + response.getBody());
        System.out.println("Status Code: " + response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
    }
}
