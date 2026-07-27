package com.kmr.marketplace.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger config.
 *
 * Swagger UI is served at /swagger-ui.html (raw spec at /v3/api-docs). A single
 * "bearerAuth" scheme lets you paste a JWT and call the authenticated endpoints:
 *  1. Expand POST /api/auth/login, "Try it out", send your email + password.
 *  2. Copy the "token" from the response.
 *  3. Click the green "Authorize" button (top right), paste the token, Authorize.
 *  4. All secured endpoints now send "Authorization: Bearer <token>" for you.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI valleyRushOpenAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Valley Rush — Marketplace API")
                        .version("v1")
                        .description("""
                                REST API for the Valley Rush multi-vendor marketplace.

                                **How to test secured endpoints:** log in via
                                `POST /api/auth/login`, copy the `token`, click
                                **Authorize** and paste it. Public endpoints
                                (auth, home, categories, product GETs) need no token.
                                Admin endpoints require a user whose role is ADMIN.""")
                        .contact(new Contact().name("Valley Rush").email("support@valleyrush.app")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by /api/auth/login")));
    }
}
