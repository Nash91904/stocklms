package com.stocklms.stock_lms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI / Swagger metadata shown in the Swagger UI.
 * Access the UI at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockLmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stock Market Trainer & Wealth Growth LMS API")
                        .description("Paper-trading and learning platform REST API. " +
                                "Simulated money only – no real financial transactions.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StockLMS Team")
                                .email("team@stocklms.dev"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
