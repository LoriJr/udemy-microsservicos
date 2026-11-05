package com.viratech.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters(SwaggerUiConfigProperties properties) {
        return new SwaggerUiConfigParameters(properties);
    }

    @Bean
    @Lazy(value = false)
    public List<GroupedOpenApi> apis(
            SwaggerUiConfigParameters config,
            RouteDefinitionLocator locator) {

        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        List<GroupedOpenApi> groups = new ArrayList<>();

        if (definitions != null) {
            definitions.stream()
                .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
                .forEach(
                        routeDefinition -> {
                            String name = routeDefinition.getId();
                            config.addGroup(name);
                            groups.add(GroupedOpenApi.builder()
                                    .group(name)
                                    .pathsToMatch("/" + name + "/**")
                                    .build()
                            );
                        }
                );
        }

        return groups;
    }
}
