package com.dtc.automation.windchill.client.download.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

@Service
@Slf4j
public class FileReaderService {

    public Set<String> readLines(String filename) {
        Path path = Paths.get(filename);
        Set<String> res;
        try {
            res = new HashSet<>(Files.readAllLines(path));
        } catch (IOException e) {
            log.error(format("Could not read file %s", filename), e);
            res = new HashSet<>();
        }
        return res;
    }
}
