package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.ObjectAttributes;
import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Service
@Slf4j
public class DocumentDownloadService {

    static final String WT_DOC_WTDOCUMENT = "wt.doc.WTDocument";

    private final ObjectService objectService;
    private final UriBuilderService uriBuilderService;
    private final RestTemplate restTemplate;
    private final FilenameUtils filenameUtils;

    public DocumentDownloadService(ObjectService objectService,
                                   UriBuilderService uriBuilderService,
                                   RestTemplate restTemplate,
                                   FilenameUtils filenameUtils) {
        this.objectService = objectService;
        this.uriBuilderService = uriBuilderService;
        this.restTemplate = restTemplate;
        this.filenameUtils = filenameUtils;
    }

    public void downloadFile(String number) {
        log.info("File download initiated for document number {}", number);
        Optional<WtDocumentObject> wtDocumentObjectOpt = objectService.fetchObject(WT_DOC_WTDOCUMENT, number);
        if (wtDocumentObjectOpt.isPresent()) {
            WtDocumentObject wtDocumentObject = wtDocumentObjectOpt.get();
            Optional<String> filenameOpt = filenameUtils.modifyFilename(wtDocumentObject);
            if (filenameOpt.isPresent()) {
                String filename = filenameOpt.get();
                Optional<URI> uri = uriBuilderService.createDownloadFileUri(wtDocumentObject.getId(), filename);
                if (uri.isPresent()) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
                    HttpEntity<String> httpEntity = new HttpEntity<>(headers);
                    log.debug("Will execute GET request to {}", uri.get());
                    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri.get(), HttpMethod.GET, httpEntity, byte[].class);
                    if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.hasBody()) {
                        log.debug("ResponseEntity: {}", responseEntity);
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
                        log.warn("Could not download file by document number {}. ResponseEntity: {}",
                                number, responseEntity);
                    }
                }
            } else {
                log.error("Could not get filename, will skip document with number {}", number);
            }
        }
    }
}
