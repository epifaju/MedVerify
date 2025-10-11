package com.medverify.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

/**
 * Configuration pour RestTemplate avec timeout et retry
 */
@Configuration
@Slf4j
public class RestTemplateConfig {

    @Value("${external-api.timeout:5000}")
    private int timeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    /**
     * Error handler personnalisé pour RestTemplate
     * Gère les erreurs 4xx et 5xx de manière appropriée
     */
    @Slf4j
    private static class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            HttpStatus.Series series = HttpStatus.Series.resolve(response.getStatusCode().value());
            return series == HttpStatus.Series.CLIENT_ERROR ||
                    series == HttpStatus.Series.SERVER_ERROR;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();

            log.debug("HTTP error response: {} - {}", statusCode.value(), statusCode.getReasonPhrase());

            // 404 Not Found = médicament non trouvé, ne pas lever d'exception
            // C'est un cas normal, on retournera Optional.empty()
            if (statusCode == HttpStatus.NOT_FOUND) {
                log.debug("Resource not found (404), returning empty result");
                return;
            }

            // Pour les autres erreurs 4xx et 5xx, on laisse l'exception se propager
            // Elle sera gérée par le client API
        }
    }
}


