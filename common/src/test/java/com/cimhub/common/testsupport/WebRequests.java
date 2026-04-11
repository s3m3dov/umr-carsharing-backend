package com.cimhub.common.testsupport;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

public final class WebRequests {

    private WebRequests() {}

    public static ServletWebRequest mockServletRequest(String uri, String headerRequestId) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(uri);
        if (headerRequestId != null) {
            servletRequest.addHeader("X-Request-Id", headerRequestId);
        }
        return new ServletWebRequest(servletRequest);
    }
}
