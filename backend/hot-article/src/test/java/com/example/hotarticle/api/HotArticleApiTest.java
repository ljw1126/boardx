package com.example.hotarticle.api;

import com.example.hotarticle.service.response.HotArticleResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Disabled
public class HotArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9004");

    @Test
    void readAllTest() {
        LocalDate today = LocalDate.now();
        String yyyyMMdd = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<HotArticleResponse> responses = restClient.get()
                .uri("/v1/hot-articles/articles/date/{dateStr}", yyyyMMdd)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        for (HotArticleResponse response : responses) {
            System.out.println("response = " + response);
        }
    }
}
