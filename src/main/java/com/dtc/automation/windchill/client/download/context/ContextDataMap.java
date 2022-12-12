package com.dtc.automation.windchill.client.download.context;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Data
public class ContextDataMap {

    //only single thread will work properly
    private final Map<String, String> data = new HashMap<>();

}
