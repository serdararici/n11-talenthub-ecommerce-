package com.n11.talenthub.order.config;

import com.iyzipay.Options;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IyzicoProperties.class)
public class IyzicoConfig {

    @Bean
    public Options iyzicoOptions(IyzicoProperties props) {
        Options options = new Options();
        options.setApiKey(props.getApiKey());
        options.setSecretKey(props.getSecretKey());
        options.setBaseUrl(props.getBaseUrl());
        return options;
    }
}
