package com.e2e.tests.user;

import com.e2e.tests.util.E2ESuite;
import com.e2e.tests.util.TestRestFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Test
    void shouldSaveUser(){
        Map<String, Object> credentialPayload = Map.of(
                "username", "pati",
                "password", "12345678",
                "roleBasedAuthority", "ROLE_USER",
                "isEnabled", true,
                "isAccountNonExpired", true,
                "isAccountNonLocked", true,
                "isCredentialsNonExpired", true
        );

        Map<String, Object> addressPayload = Map.of(
                "fullAddress", "Calle 35 norte #6a abis 35",
                "postalCode", "760001",
                "city", "Cali"
        );

        Map<String, Object> userPayload = Map.of(
                "firstName", "Juan David",
                "lastName", "Patino",
                "imageUrl", "http://placeholder:200",
                "email", "juandi2321@gmial.com",
                "phone", "3126110876",
                "addressDtos", List.of(addressPayload),
                "credential", credentialPayload
        );
        ResponseEntity<String> response = restFacade.post(userServiceUrl + "/user-service/api/users",
                userPayload,
                String.class);
        System.out.println("Response: " + response.getBody());
        System.out.println("Status Code: " + response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
    }
}
