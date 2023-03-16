package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.ObjectAttributes;
import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FilenameUtils {

    public Optional<String> modifyFilename(WtDocumentObject wtDocumentObject) {
        ObjectAttributes objectAttributes = wtDocumentObject.getObjectAttributes();
        Pattern pattern = Pattern.compile("^(.+)\\.(\\w+)$");
        Matcher matcher = pattern.matcher(objectAttributes.getFileName());
        Optional<String> res = Optional.empty();
        if (matcher.matches()) {
            String docName = matcher.group(1);
            String extension = matcher.group(2);
            res = Optional.of(docName + ", " + objectAttributes.getVersion() + " " + objectAttributes.getState() + "." + extension);
        } else {
            log.warn("Could not get filename for document {}", wtDocumentObject);
        }
        return res;
    }
}
