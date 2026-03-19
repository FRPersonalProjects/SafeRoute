package com.saferoute.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

  @Bean
  public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {

    return builder.routes() // definir as rotas
        // .route(p -> p.path("/get") // definir o caminho da rota
        // .filters(f -> f
        // .addRequestHeader("Hello", "World") // adicionar um header à requisição
        // .addRequestParameter("Hello", "World")) // adicionar um parâmetro à
        // requisição
        // .uri("http://httpbin.org:80")) // definir o destino da rota

        .route(p -> p.path("/book-service/**").uri("lb://book-service")) // lb = load balancer, nome do serviço
                                                                         // registrado no Eureka
        .route(p -> p.path("/exchange-service/**").uri("lb://exchange-service")) // lb = load balancer, nome do serviço
                                                                                 // registrado no Eureka
        .build();
  }

}
