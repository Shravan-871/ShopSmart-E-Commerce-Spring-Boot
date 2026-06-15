package com.shopsmart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopsmartOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopSmart E-Commerce API")
                        .description("REST API for products, cart, and orders")
                        .version("1.0"));
    }
}
