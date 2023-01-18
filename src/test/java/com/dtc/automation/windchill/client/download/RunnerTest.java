package com.dtc.automation.windchill.client.download;

import com.dtc.automation.windchill.client.download.service.DocumentDownloadService;
import com.dtc.automation.windchill.client.download.service.FileReaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.dtc.automation.windchill.client.download.Runner.FILENAME;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunnerTest {

    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private FileReaderService fileReaderService;

    @InjectMocks
    private Runner runner;

    @Test
    public void shouldTrimDocumentNumberForDownloadService() throws Exception {
        Set<String> numbers = new HashSet<>(Collections.singletonList("  123\t"));
        when(fileReaderService.readLines(FILENAME)).thenReturn(numbers);

        runner.run("");

        verify(documentDownloadService).downloadFile("123");
    }

    @Test
    public void shouldIgnoreEmptyLinesFromDocumentNumbersList() throws Exception {
        Set<String> numbers = new HashSet<>(Arrays.asList("\t", "  ", "123", ""));
        when(fileReaderService.readLines(FILENAME)).thenReturn(numbers);

        runner.run("");

        verify(documentDownloadService, times(1)).downloadFile(anyString());
        verify(documentDownloadService).downloadFile("123");
    }

}