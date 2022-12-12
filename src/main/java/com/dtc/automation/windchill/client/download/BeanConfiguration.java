package com.dtc.automation.windchill.client.download;

import com.dtc.automation.windchill.client.download.context.ContextDataMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BeanConfiguration {

    @Value("${name}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${windchill.auth.token}")
    private String token;

    @Bean
    public RestTemplate payloadRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        String authEncoded;
        if (StringUtils.isNotBlank(token)) {
            authEncoded = token;
        } else {
            String auth = username + ":" + password;
            authEncoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        }
        String authHeader = "Basic " + authEncoded;
        return new RestTemplateBuilder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(ContextDataMap contextDataMap) {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    public boolean isRedirected(HttpRequest request, HttpResponse response,
                                                HttpContext context) throws ProtocolException {
                        if (super.isRedirected(request, response, context)) {
                            int statusCode = response.getStatusLine().getStatusCode();
                            String redirectURL = response.getFirstHeader("Location").getValue();
                            if (statusCode == 303 && redirectURL != null && redirectURL.startsWith("https://windchill.jnj.com/Windchill/servlet/WindchillAuthGW/wt.content.ContentHttp/viewContent/")) {
                                List<NameValuePair> uriParams = URLEncodedUtils.parse(redirectURL, StandardCharsets.UTF_8);
                                for (NameValuePair p : uriParams) {
                                    if ("ofn".equals(p.getName())) {
                                        contextDataMap.getData().put("_filename", p.getValue());
                                        break;
                                    }
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                })
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
