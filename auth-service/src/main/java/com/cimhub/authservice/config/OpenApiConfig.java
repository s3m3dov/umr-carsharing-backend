package com.cimhub.authservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${springdoc.server.url:/auth-service}")
        private String serverUrl;

	@Bean
	public OpenAPI authServiceOpenAPI() {
		Server server = new Server();
		server.setUrl(serverUrl);
		server.setDescription("Auth Service");

		Contact contact = new Contact();
		contact.setEmail("Samadov@students.uni-marburg.de");
		contact.setName("Hikmat Samadov");

		License license = new License()
				.name("Apache 2.0")
				.url("http://www.apache.org/licenses/LICENSE-2.0.html");

		Info info = new Info()
				.title("Auth Service API")
				.version("1.0.0")
				.contact(contact)
				.description("Authentication and authorization service for the Car Sharing platform")
				.license(license);

		SecurityScheme securityScheme = new SecurityScheme()
				.name("Bearer Authentication")
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.in(SecurityScheme.In.HEADER)
				.description("Enter JWT token obtained from /api/auth/login or /api/auth/signup");

		SecurityRequirement securityRequirement = new SecurityRequirement()
				.addList("Bearer Authentication");

		return new OpenAPI()
				.info(info)
				.servers(List.of(server))
				.components(new Components()
						.addSecuritySchemes("Bearer Authentication", securityScheme));
	}
}
