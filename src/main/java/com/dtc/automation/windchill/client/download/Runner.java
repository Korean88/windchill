package com.dtc.automation.windchill.client.download;

import com.dtc.automation.windchill.client.download.service.WindchillDownloaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashSet;

@SpringBootApplication
@Slf4j
public class Runner implements CommandLineRunner {

    private static final String EXCEL_FILE_NAME = "doc_numbers.xlsx";

    @Autowired
    private WindchillDownloaderService windchillDownloaderService;

    public static void main(String[] args) {
        readTokenFile();

        System.setProperty("javax.net.ssl.trustStore", "trust.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SpringApplication.run(Runner.class, args).close();
    }

    private static void readTokenFile() {
        Path tokenPath = Paths.get("token.temp");
        if (Files.exists(tokenPath)) {
            try {
                HashSet<String> lines = new HashSet<>(Files.readAllLines(tokenPath));
                if (lines.iterator().hasNext()) {
                    System.setProperty("token.encoded", lines.iterator().next());
                } else {
                    setLoginCreds(tokenPath);
                }
            } catch (IOException e) {
                log.error("Could not read token from token.temp", e);
            }
        } else {
            setLoginCreds(tokenPath);
        }
    }

    private static void setLoginCreds(Path tokenPath) {
        Console console = System.console();
        StringBuilder name = new StringBuilder(console.readLine("Enter username: "));
        char[] password = console.readPassword("Enter password: ");
        String saveToFile = console.readLine("Save credentials? [y|n]: ");
        name.append(":").append(password);
        String tokenEncoded = Base64.getEncoder().encodeToString(name.toString().getBytes(StandardCharsets.UTF_8));
        System.setProperty("token.encoded", tokenEncoded);

        if ("y".equalsIgnoreCase(saveToFile)) {
            try {
                Files.write(tokenPath, tokenEncoded.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.error("Could not save token", e);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        windchillDownloaderService.processDocNumbersFromExcel(EXCEL_FILE_NAME, args[0]);
    }
}
