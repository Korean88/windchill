package com.dtc.automation.windchill.client.download.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UriBuilderServiceTest {

    private final UriBuilderService uriBuilderService = new UriBuilderService();

    @Test
    public void shouldCreateUriForFetchObjectIdIfTypeIdAndDocumentNumberProvided() {
        String typeId = "typeId";
        String docNumber = "012";

        Optional<URI> fetchObjectIdUri = uriBuilderService.createFetchObjectIdUri(typeId, docNumber);

        assertTrue(fetchObjectIdUri.isPresent());
        assertEquals("https://windchill.jnj.com:443/Windchill/servlet/rest/search/objects?typeId=typeId&$select=state,version,fileName&$filter=number%20eq%20'012'",
                fetchObjectIdUri.get().toString());
    }

    @ParameterizedTest
    @MethodSource("emptyTypeIdAndDocNumber")
    public void shouldReturnEmptyOptionalIfTypeIdAndDocumentNumberNotProvided(String typeId, String docNumber) {
        Optional<URI> fetchObjectIdUri = uriBuilderService.createFetchObjectIdUri(typeId, docNumber);

        assertFalse(fetchObjectIdUri.isPresent());
    }

    private static Stream<Arguments> emptyTypeIdAndDocNumber() {
        return Stream.of(Arguments.of("", ""),
                Arguments.of("", "012"),
                Arguments.of(null, "012"),
                Arguments.of("typeId", ""),
                Arguments.of("typeId", null),
                Arguments.of(null, null)
        );
    }

    @Test
    public void shouldCreateUriForDownloadFileEndpointIfIdAndFilenameProvided() {
        String id = "OR:wt.doc.WTDocument:docId";
        String filename = "random.doc";

        Optional<URI> fetchObjectIdUri = uriBuilderService.createDownloadFileUri(id, filename);

        assertTrue(fetchObjectIdUri.isPresent());
        assertEquals("https://windchill.jnj.com:443/Windchill/servlet/rest/files/downloadDoc/OR:wt.doc.WTDocument:docId/random.doc", fetchObjectIdUri.get().toString());
    }

    @ParameterizedTest
    @MethodSource("emptyIdAndFilename")
    public void shouldReturnEmptyOptionalIfIdAndFilenameNotProvided(String id, String filename) {
        Optional<URI> fetchObjectIdUri = uriBuilderService.createDownloadFileUri(id, filename);

        assertFalse(fetchObjectIdUri.isPresent());
    }

    private static Stream<Arguments> emptyIdAndFilename() {
        return Stream.of(Arguments.of("", ""),
                Arguments.of("", "012"),
                Arguments.of(null, "012"),
                Arguments.of("typeId", ""),
                Arguments.of("typeId", null),
                Arguments.of(null, null)
        );
    }

}