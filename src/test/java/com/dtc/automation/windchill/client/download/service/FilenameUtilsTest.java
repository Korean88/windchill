package com.dtc.automation.windchill.client.download.service;

import com.dtc.automation.windchill.client.download.model.ObjectAttributes;
import com.dtc.automation.windchill.client.download.model.WtDocumentObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FilenameUtilsTest {

    private final FilenameUtils filenameUtils = new FilenameUtils();

    @Test
    public void shouldAppendVersionAndStateToOriginalFilename_ifFilenameValid() {
        ObjectAttributes objectAttributes = new ObjectAttributes();
        String origFilename = "103541130-Rev-F---CONDUIT-LLIF-RTM";
        objectAttributes.setFileName(origFilename + ".xlsx");
        objectAttributes.setState("In work");
        objectAttributes.setVersion("V.5");
        WtDocumentObject wtDocumentObject = new WtDocumentObject();
        wtDocumentObject.setObjectAttributes(objectAttributes);
        wtDocumentObject.setId("test_id_1");
        wtDocumentObject.setTypeId("test_type_id_1");

        Optional<String> resultOpt = filenameUtils.modifyFilename(wtDocumentObject);

        assertTrue(resultOpt.isPresent());
        assertEquals(origFilename + ", " + "V.5 In work.xlsx", resultOpt.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"103541130-Rev-F---CONDUIT-LLIF-RTM", "test 123 doc", "test 123,doc", "test 123_doc"})
    public void shouldReturnEmptyOptional_ifOriginalFilenameNotValid(String origFilename) {
        ObjectAttributes objectAttributes = new ObjectAttributes();
        objectAttributes.setFileName(origFilename);
        objectAttributes.setState("In work");
        objectAttributes.setVersion("V.5");
        WtDocumentObject wtDocumentObject = new WtDocumentObject();
        wtDocumentObject.setObjectAttributes(objectAttributes);
        wtDocumentObject.setId("test_id_1");
        wtDocumentObject.setTypeId("test_type_id_1");

        Optional<String> resultOpt = filenameUtils.modifyFilename(wtDocumentObject);

        assertFalse(resultOpt.isPresent());
    }
}
