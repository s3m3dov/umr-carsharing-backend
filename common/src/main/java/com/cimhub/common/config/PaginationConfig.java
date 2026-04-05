package com.cimhub.common.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@ConfigurationProperties(prefix = "pagination")
public class PaginationConfig {

    @NotNull
    @Min(1)
    private Integer defaultPageSize;

    @NotNull
    @Min(1)
    private Integer maxPageSize;

    @NotNull
    @Min(1)
    private Integer minPageSize;

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(Integer defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public int getMinPageSize() {
        return minPageSize;
    }

    public void setMinPageSize(Integer minPageSize) {
        this.minPageSize = minPageSize;
    }
}
