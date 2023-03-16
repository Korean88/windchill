package com.dtc.automation.windchill.client.download.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SearchObjectResponse {

    @JsonProperty(value = "items")
    private List<WtDocumentObject> items;

}
