package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ObjectService {

    private final RestTemplate restTemplate;
    private final UriBuilderService uriBuilderService;

    public ObjectService(RestTemplate restTemplate,
                         UriBuilderService uriBuilderService) {
        this.restTemplate = restTemplate;
        this.uriBuilderService = uriBuilderService;
    }

    public Optional<String> fetchObjectId(String typeId, String number) {
        Optional<String> objectId = Optional.empty();
        Optional<URI> fetchObjectIdUri = uriBuilderService.createFetchObjectIdUri(typeId, number);
        if (fetchObjectIdUri.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            log.debug("Will execute GET request to {}", fetchObjectIdUri.get());
            ResponseEntity<List<WtDocumentObject>> responseEntity = restTemplate
                    .exchange(fetchObjectIdUri.get(), HttpMethod.GET, httpEntity,
                            new ParameterizedTypeReference<List<WtDocumentObject>>() {
                            });
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                List<WtDocumentObject> documentObjects = responseEntity.getBody();
                if (!CollectionUtils.isEmpty(documentObjects)) {
//                    if (documentObjects.size() > 1) {
//                        log.warn("Returned more than 1 documents with number {}. Documents returned: {}. " +
//                                "Will skip...", number, documentObjects);
//                    } else {
                        objectId = Optional.of(documentObjects.get(0).getId());
//                    }
                } else {
                    log.warn("No document ids were received for document number {}. Will skip...", number);
                }
            } else {
                log.warn("Fetch Object Id by number {} failed. {}", number, responseEntity);
            }
        }
        return objectId;
    }
}
