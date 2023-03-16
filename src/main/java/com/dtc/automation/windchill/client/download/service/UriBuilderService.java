package com.dtc.automation.windchill.client.download.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
@Slf4j
public class UriBuilderService {

    public Optional<URI> createFetchObjectIdUri(String typeId, String documentNumber) {
        Optional<URI> res = Optional.empty();
        if (!StringUtils.isBlank(typeId) && !StringUtils.isBlank(documentNumber)) {
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("windchill.jnj.com")
                    .port(443)
                    .path("Windchill/servlet/rest/search/objects")
                    .queryParam("typeId", "{typeId}")
                    .queryParam("$select", "state,version,fileName")
                    .queryParam("$filter", "number eq '{documentNumber}'")
                    .buildAndExpand(typeId, documentNumber);
            res = Optional.of(uriComponents.toUri());
        } else {
            log.warn("Document number or typeId is empty, will skip. typeId={}, documentNumber={}", typeId, documentNumber);
        }
        log.debug("object id url for docNumber {}: {}", documentNumber, res.orElse(null));
        return res;
    }

    public Optional<URI> createDownloadFileUri(String id, String filename) {
        Optional<URI> res = Optional.empty();
        if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(filename)) {
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("windchill.jnj.com")
                    .port(443)
                    .path("Windchill/servlet/rest/files/downloadDoc/")
                    .path(id)
                    .path("/")
                    .path(filename)
                    .build();
            res = Optional.of(uriComponents.toUri());
        } else {
            log.warn("Document id or filename is empty, will skip. id={}, filename={}", id, filename);
        }
        log.debug("download url for id {}: {}", id, res.orElse(null));
        return res;
    }
}
