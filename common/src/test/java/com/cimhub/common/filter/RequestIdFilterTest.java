package com.cimhub.common.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void doFilterInternal_withExistingHeader_shouldReuseRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String requestId = "req-123";
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, requestId);

        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcInsideChain.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertEquals(requestId, response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
        assertEquals(requestId, mdcInsideChain.get());
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    @Test
    void doFilterInternal_withoutHeader_shouldGenerateRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcInsideChain.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        String generated = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertNotNull(generated);
        UUID.fromString(generated);
        assertEquals(generated, mdcInsideChain.get());
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }
}
