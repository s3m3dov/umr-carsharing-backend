//package com.singhv.apigateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        // Option 1: Keep credentials and use specific origins
////        corsConfig.setAllowedOrigins(Arrays.asList(
////                "https://tanzania-car-sharing-1.onrender.com",
////                "http://localhost:3000",
////                "https://localhost:3000",
////                "http://localhost:8080"
////        ));
//
//
//         //Option 2 (alternative): If you prefer wildcard with no credentials
//         corsConfig.setAllowedOrigins(Arrays.asList("*"));
//         corsConfig.setAllowCredentials(false);
//
//        corsConfig.setMaxAge(3600L);
//        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        corsConfig.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return new CorsWebFilter(source);
//    }
//}