package com.clinic.infrastructure.config;

import com.clinic.application.port.out.CoverageVerificationPort;
import com.clinic.infrastructure.coverage.RestCoverageVerificationAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Chooses the coverage adapter: clinic.coverage.mode = rest | stub (default). */
@Configuration
public class CoverageConfig {

    @Bean
    @ConditionalOnProperty(name = "clinic.coverage.mode", havingValue = "rest")
    CoverageVerificationPort restCoverage(@Value("${clinic.coverage.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return new RestCoverageVerificationAdapter(
                RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build());
    }

    @Bean
    @ConditionalOnProperty(name = "clinic.coverage.mode", havingValue = "stub", matchIfMissing = true)
    CoverageVerificationPort stubCoverage() {
        return documentId -> true;
    }
}
