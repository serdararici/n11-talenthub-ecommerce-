package com.n11.talenthub.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iyzico")
@Data
public class IyzicoProperties {

    private String apiKey;
    private String secretKey;
    private String baseUrl;
}
