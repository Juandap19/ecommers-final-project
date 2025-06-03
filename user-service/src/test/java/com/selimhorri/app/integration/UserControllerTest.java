package com.selimhorri.app.integration;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.unit.util.UserUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver"})
                
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest  {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateUser() {
        String url = "http://localhost:" + port + "/user-service/api/users";
        UserDto userDto = UserUtil.getSampleUserDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto.getFirstName(), response.getBody().getFirstName());
        assertEquals(userDto.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void testGetUserById() {
        Integer userId = 1;
        String url = "http://localhost:" + port + "/user-service/api/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserDto.class
        );

        System.out.println("Response: " + response.getBody());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetAllUsers() {
        String url = "http://localhost:" + port + "/user-service/api/users";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}