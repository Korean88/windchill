package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.SearchObjectResponse;
import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObjectServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UriBuilderService uriBuilderService;

    @InjectMocks
    private ObjectService objectService;

    @Test
    public void shouldReturnFirstDocumentObjectIfTwoObjectsReturnedFromEndpoint() throws IOException {
        String typeId = "type";
        String number = "123";
        URI fetchObjectUri = URI.create("https://dummy.url");
        when(uriBuilderService.createFetchObjectIdUri(typeId, number)).thenReturn(Optional.of(fetchObjectUri));
        ObjectMapper objectMapper = new ObjectMapper();
        SearchObjectResponse searchObjectResponse = objectMapper.readValue(
                this.getClass().getClassLoader().getResourceAsStream("objects.json"), SearchObjectResponse.class);
        when(restTemplate.exchange(eq(fetchObjectUri), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity(searchObjectResponse, HttpStatus.OK));

        Optional<WtDocumentObject> wtDocumentObject = objectService.fetchObject(typeId, number);

        assertTrue(wtDocumentObject.isPresent());
    }
}
