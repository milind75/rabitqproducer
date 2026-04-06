package com.order.queue.rabitq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rabbitmq")
@Tag(name = "RabbitMQ", description = "Publish messages to RabbitMQ queues")
public class RabbitMqController {

    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public RabbitMqController(RabbitTemplate rabbitTemplate,
                              @Value("${app.rabbitmq.queue}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish a message", description = "Publishes a message body to the configured RabbitMQ queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message published"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "503", description = "RabbitMQ unavailable or connection failed")
    })
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody PublishMessageRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message must not be blank"));
        }

        try {
            rabbitTemplate.convertAndSend(queueName, request.message());
            return ResponseEntity.ok(Map.of("status", "Message published", "queue", queueName));
        } catch (AmqpException ex) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Failed to publish message to RabbitMQ",
                    "queue", queueName,
                    "details", ex.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/publish-with-key")
    @Operation(summary = "Publish a message with routing key", description = "Publishes a message with a custom routing key to the default exchange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message published"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "503", description = "RabbitMQ unavailable or connection failed")
    })
    public ResponseEntity<Map<String, String>> publishMessageWithKey(@RequestBody PublishMessageWithKeyRequest request) {
        if (request == null || request.key() == null || request.key().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "key must not be blank"));
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message must not be blank"));
        }

        try {
            rabbitTemplate.convertAndSend("", request.key(), request.message());
            return ResponseEntity.ok(Map.of(
                    "status", "Message published",
                    "routingKey", request.key()
            ));
        } catch (AmqpException ex) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Failed to publish message to RabbitMQ",
                    "routingKey", request.key(),
                    "details", ex.getClass().getSimpleName()
            ));
        }
    }

    public record PublishMessageRequest(
            @Schema(description = "Message payload to publish", example = "Order created: 12345")
            String message
    ) {
    }

    public record PublishMessageWithKeyRequest(
            @Schema(description = "Routing key for message", example = "order.created")
            String key,
            @Schema(description = "Message payload to publish", example = "Order created: 12345")
            String message
    ) {
    }
}

