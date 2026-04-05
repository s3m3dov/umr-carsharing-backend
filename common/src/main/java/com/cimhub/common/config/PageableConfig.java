package com.cimhub.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@RequiredArgsConstructor
public class PageableConfig {

    private final PaginationConfig paginationConfig;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setMaxPageSize(paginationConfig.getMaxPageSize());
            Pageable fallback = PageRequest.of(0, paginationConfig.getDefaultPageSize());
            resolver.setFallbackPageable(fallback);
        };
    }
}
