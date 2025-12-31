package com.cimhub.notificationservice.config;

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

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Notification Service - Development");

        Contact contact = new Contact();
        contact.setEmail("Samadov@students.uni-marburg.de");
        contact.setName("Hikmat Samadov");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Notification Service API")
                .version("1.0.0")
                .contact(contact)
                .description("Real-time notification and WebSocket service for the Car Sharing platform")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
