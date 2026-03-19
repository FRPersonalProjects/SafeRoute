package com.saferoute.sensor_service.config;

import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;

@OpenAPIDefinition(info = @Info(title = "Book Microservice API", version = "1", description = "API for managing books"))
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components())
        .info(new io.swagger.v3.oas.models.info.Info()
            .title("Book Microservice API")
            .version("1.0")
            .license(new License()
                .name("")
                .url("")));
  }

}
