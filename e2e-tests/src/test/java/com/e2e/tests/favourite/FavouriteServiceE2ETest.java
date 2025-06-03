package com.e2e.tests.favourite;

import com.e2e.tests.util.E2ESuite;
import com.e2e.tests.util.TestRestFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FavouriteServiceE2ETest extends E2ESuite {
    private static final String EXPECTED_BACKEND_DATE_TIME_FORMAT = "dd-MM-yyyy__HH:mm:ss:SSSSSS";

    @Autowired
    private TestRestFacade restFacade;

    @Value("${favourite.service.url}")
    private String favouriteServiceUrl;


    @Test
    void shouldGetAllFavourites() {
        ResponseEntity<String> response = restFacade.get(
                favouriteServiceUrl + "/favourite-service/api/favourites",
                String.class);
        System.out.println("Response: " + response.getBody());
        System.out.println("Status Code: " + response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
    }

//    @Test
//    void shoulgGetFavouriteById() {
//        String userId = "1";
//        String productId = "2";
//        LocalDateTime likeDateTime = LocalDateTime.of(2025, 5, 25, 7, 31, 51, 559551000);
//        String likeDate = likeDateTime.format(DateTimeFormatter.ofPattern(EXPECTED_BACKEND_DATE_TIME_FORMAT));
//        ResponseEntity<String> response = restFacade.get(
//                favouriteServiceUrl + "/favourite-service/api/favourites/" + userId + "/" + productId + "/" + likeDate,
//                String.class);
//        System.out.println("Response: " + response.getBody());
//        System.out.println("Status Code: " + response.getStatusCode());
//        assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
//    }

}
