package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.ObjectAttributes;
import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectService objectService;
    @Mock
    private UriBuilderService uriBuilderService;
    @Mock
    private FilenameUtils filenameUtils;

    @InjectMocks
    private DocumentDownloadService documentDownloadService;

    @Test
    public void shouldDownloadFileIfFound() throws IOException {
        String number = "012";
        String docId = "345";
        WtDocumentObject wtDocumentObject = createWtDocumentObject(docId);
        String filename = "document, V.5 In work.txt";
        when(filenameUtils.modifyFilename(wtDocumentObject, number)).thenReturn(Optional.of(filename));
        when(objectService.fetchObject(DocumentDownloadService.WT_DOC_WTDOCUMENT, number))
                .thenReturn(Optional.of(wtDocumentObject));
        URI downloadUri = URI.create("https://windchill.jnj.com/download");
        when(uriBuilderService.createDownloadFileUri(docId, filename))
                .thenReturn(Optional.of(downloadUri));
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<byte[]> byteResponseEntity = new ResponseEntity<>("abc".getBytes(StandardCharsets.UTF_8), responseHeaders, HttpStatus.OK);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
        when(restTemplate.exchange(downloadUri, HttpMethod.GET, requestEntity, byte[].class))
                .thenReturn(byteResponseEntity);
        Path filePath = Paths.get("download", number, filename);

        documentDownloadService.downloadFile(number);

        assertTrue(Files.exists(filePath));
        Files.delete(Paths.get(filePath.toString()));
        Files.delete(Paths.get("download", number));
        Files.delete(Paths.get("download"));
    }

    @Test
    public void shouldNotDownloadFileIfEndpointReturns500() {
        String number = "012";
        String docId = "345";
        WtDocumentObject wtDocumentObject = createWtDocumentObject(docId);
        String filename = "document, V.5 In work.txt";
        when(filenameUtils.modifyFilename(wtDocumentObject, number)).thenReturn(Optional.of(filename));
        when(objectService.fetchObject(DocumentDownloadService.WT_DOC_WTDOCUMENT, number))
                .thenReturn(Optional.of(wtDocumentObject));
        URI downloadUri = URI.create("https://windchill.jnj.com/download");
        when(uriBuilderService.createDownloadFileUri(docId, filename))
                .thenReturn(Optional.of(downloadUri));
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<byte[]> byteResponseEntity = new ResponseEntity<>("abc".getBytes(StandardCharsets.UTF_8), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
        when(restTemplate.exchange(downloadUri, HttpMethod.GET, requestEntity, byte[].class))
                .thenReturn(byteResponseEntity);

        documentDownloadService.downloadFile(number);

        assertFalse(Files.exists(Paths.get("download")));
    }

    private WtDocumentObject createWtDocumentObject(String docId) {
        WtDocumentObject wtDocumentObject = new WtDocumentObject();
        wtDocumentObject.setId(docId);
        ObjectAttributes objectAttributes = new ObjectAttributes();
        objectAttributes.setFileName("dcoument.txt");
        objectAttributes.setVersion("V.5");
        objectAttributes.setState("In work");
        wtDocumentObject.setObjectAttributes(objectAttributes);
        return wtDocumentObject;
    }

}