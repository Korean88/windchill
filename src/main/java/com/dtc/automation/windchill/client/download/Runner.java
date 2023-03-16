package com.dtc.automation.windchill.client.download;

import com.dtc.automation.windchill.client.download.service.DocumentDownloadService;
import com.dtc.automation.windchill.client.download.service.FileReaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

@SpringBootApplication
@Slf4j
public class Runner implements CommandLineRunner {

    static final String FILENAME = "doc_numbers.txt";

    @Autowired
    private DocumentDownloadService documentDownloadService;
    @Autowired
    private FileReaderService fileReaderService;

    public static void main(String[] args) {
        Path tokenPath = Paths.get("token.temp");
        if (Files.exists(tokenPath)) {
            try {
                HashSet<String> lines = new HashSet<>(Files.readAllLines(tokenPath));
                if (lines.iterator().hasNext()) {
                    System.setProperty("token.encoded", lines.iterator().next());
                } else {
                    setLoginCreds();
                }
            } catch (IOException e) {
                log.error("Could not read token from token.temp", e);
            }
        } else {
            setLoginCreds();
        }

        System.setProperty("javax.net.ssl.trustStore", "trust.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SpringApplication.run(Runner.class, args).close();
    }

    private static void setLoginCreds() {
        Console console = System.console();
        StringBuilder name = new StringBuilder(console.readLine("Enter username: "));
        char[] password = console.readPassword("Enter password: ");
        name.append(":").append(password);
        String tokenEncoded = Base64.getEncoder().encodeToString(name.toString().getBytes(StandardCharsets.UTF_8));
        System.setProperty("token.encoded", tokenEncoded);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<String> docNumbers = fileReaderService.readLines(FILENAME);
        if (!CollectionUtils.isEmpty(docNumbers)) {
                docNumbers.stream()
                        .filter(s -> !StringUtils.isBlank(s))
                        .map(String::trim)
                        .forEach( number -> {
                            try {
                                documentDownloadService.downloadFile(number);
                            } catch (Exception e) {
                                log.error(format("Failed to download document with number %s", number), e);
                            }
                        });
        } else {
            log.warn("The file {} is empty. Please provide document numbers in the file", FILENAME);
        }
    }
}
