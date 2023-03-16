package com.dtc.automation.windchill.client.download.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ObjectAttributes {

    @JsonProperty(value = "state")
    private String state;
    @JsonProperty(value = "fileName")
    private String fileName;
    @JsonProperty(value = "version")
    private String version;
}
