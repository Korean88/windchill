package com.dtc.automation.windchill.client.download.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WtDocumentObject {

    @JsonProperty(value = "typeId")
    private String typeId;
    @JsonProperty(value = "id")
    private String id;

}
