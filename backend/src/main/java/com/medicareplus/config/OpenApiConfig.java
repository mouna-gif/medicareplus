package com.medicareplus.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.*;
import org.springdoc.core.customizers.OperationCustomizer;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI medicarePlusOpenApi() {
        String scheme = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("MediCare+ REST API")
                .version("1.0.0")
                .description("Secure healthcare management API backed by Spring Boot and MySQL 8."))
            .components(new Components().addSecuritySchemes(scheme, new SecurityScheme()
                .name(scheme)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(scheme));
    }

    @Bean
    OperationCustomizer standardApiResponses() {
        return (operation, handlerMethod) -> {
            if (operation.getDescription() == null || operation.getDescription().isBlank()) {
                operation.setDescription(operation.getSummary());
            }
            addResponse(operation, "400", "Request validation failed");
            addResponse(operation, "401", "Authentication is required");
            addResponse(operation, "403", "The authenticated role is not authorized");
            addResponse(operation, "404", "The requested resource does not exist");
            addResponse(operation, "409", "A MediCare+ business rule was violated");
            return operation;
        };
    }

    private void addResponse(io.swagger.v3.oas.models.Operation operation, String code, String description) {
        if (operation.getResponses() == null) operation.setResponses(new ApiResponses());
        operation.getResponses().addApiResponse(code, new ApiResponse()
            .description(description)
            .content(new Content().addMediaType("application/json", new MediaType())));
    }
}
