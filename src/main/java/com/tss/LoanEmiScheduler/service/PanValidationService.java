package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.dto.response.UserDetailsFetchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PanValidationService {

    private final RestClient restClient = RestClient.create("http://localhost:8081");

    public UserDetailsFetchDto fetchDetailsFromExternalSystem(String panCard) {
        return restClient.get()
                .uri("/api/population-details/{panCard}", panCard)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("No record found for PAN: " + panCard);
                })
                .body(UserDetailsFetchDto.class);
    }
}
