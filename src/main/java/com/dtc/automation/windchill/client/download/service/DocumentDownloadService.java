package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.context.ContextDataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;

@Service
@Slf4j
public class DocumentDownloadService {

    static final String WT_DOC_WTDOCUMENT = "wt.doc.WTDocument";

    private final ObjectService objectService;
    private final UriBuilderService uriBuilderService;
    private final RestTemplate restTemplate;
    private final ContextDataMap contextDataMap;

    public DocumentDownloadService(ObjectService objectService,
                                   UriBuilderService uriBuilderService,
                                   RestTemplate restTemplate,
                                   ContextDataMap contextDataMap) {
        this.objectService = objectService;
        this.uriBuilderService = uriBuilderService;
        this.restTemplate = restTemplate;
        this.contextDataMap = contextDataMap;
    }

    public void downloadFile(String number) {
        log.info("File download initiated for document number {}", number);
        Optional<String> objectId = objectService.fetchObjectId(WT_DOC_WTDOCUMENT, number);
        if (objectId.isPresent()) {
            Optional<URI> uri = uriBuilderService
                    .createDownloadFileUri(objectId.get(), number + ".doc");
            if (uri.isPresent()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
                HttpEntity<String> httpEntity = new HttpEntity<>(headers);
                log.debug("Will execute GET request to {}", uri.get());
                ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri.get(), HttpMethod.GET, httpEntity, byte[].class);
                if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.hasBody()) {
                    log.debug("ResponseEntity: {}", responseEntity);
                    Optional<String> filenameOpt = Optional.of(contextDataMap.getData())
                            .map(d -> d.get("_filename"));
                    contextDataMap.getData().remove("_filename");
                    if (filenameOpt.isPresent()) {
                        String filename = filenameOpt.get();
                        try {
                            Path downloadPath = Paths.get("download", number);
                            Files.createDirectories(downloadPath);
                            Path filePath = Paths.get(downloadPath.toString(), filename);
                            Files.write(filePath, responseEntity.getBody(), StandardOpenOption.CREATE);
                            log.info("Successfully stored file {} for document number {}", filename, number);
                        } catch (IOException e) {
                            log.error(format("Could not save file %s. Document number: %s", filename, number), e);
                        }
                    } else {
                        log.warn("Filename is missing for document number {}. Will skip...", number);
                    }
                } else {
                    log.warn("Could not download file by document number {}. ResponseEntity: {}",
                            number, responseEntity);
                }
            }
        }
    }
}
