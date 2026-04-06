package com.order.queue.rabitq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rabbitMqProducerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RabbitMQ Producer API")
                        .version("v1")
                        .description("APIs to publish messages to RabbitMQ."));
    }
}

