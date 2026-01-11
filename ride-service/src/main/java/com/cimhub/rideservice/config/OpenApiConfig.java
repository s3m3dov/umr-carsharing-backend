package com.cimhub.rideservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${springdoc.server.url:/ride-service}")
        private String serverUrl;

        @Bean
        public OpenAPI rideServiceOpenAPI() {
                Server server = new Server();
                server.setUrl(serverUrl);
                server.setDescription("Ride Service");

                Contact contact = new Contact();
                contact.setEmail("Samadov@students.uni-marburg.de");
                contact.setName("Hikmat Samadov");

                License license = new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

                Info info = new Info()
                                .title("Ride Service API")
                                .version("1.0.0")
                                .contact(contact)
                                .description("Ride finding and joining service for the Car Sharing platform")
                                .license(license);

                return new OpenAPI()
                                .info(info)
                                .servers(List.of(server));
        }
}
