package com.dtc.automation.windchill.client.download.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NonceToken {

    @JsonProperty(value = "@odata.context")
    private String dataContext;
    @JsonProperty(value = "NonceKey")
    private String nonceKey;
    @JsonProperty(value = "NonceValue")
    private String nonceValue;

}
