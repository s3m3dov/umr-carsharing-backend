package com.cimhub.tripservice.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OSMClientConfigTest {

    @Test
    void webClient_shouldApplyConfiguredBaseUrlAndMemoryLimit() {
        OSMClientConfig config = new OSMClientConfig();
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_SELF);
        WebClient webClient = mock(WebClient.class);
        when(builder.build()).thenReturn(webClient);

        WebClient configured = config.webClient(builder);

        assertNotNull(configured);
        verify(builder).baseUrl("https://router.project-osrm.org/route/v1/driving/");

        ArgumentCaptor<ExchangeStrategies> strategiesCaptor = ArgumentCaptor.forClass(ExchangeStrategies.class);
        verify(builder).exchangeStrategies(strategiesCaptor.capture());

        int maxInMemorySize = strategiesCaptor.getValue().messageReaders().stream()
                .filter(DecoderHttpMessageReader.class::isInstance)
                .map(DecoderHttpMessageReader.class::cast)
                .map(DecoderHttpMessageReader::getDecoder)
                .filter(AbstractDataBufferDecoder.class::isInstance)
                .map(AbstractDataBufferDecoder.class::cast)
                .mapToInt(AbstractDataBufferDecoder::getMaxInMemorySize)
                .max()
                .orElse(-1);

        assertEquals(2 * 1024 * 1024, maxInMemorySize);
    }
}
