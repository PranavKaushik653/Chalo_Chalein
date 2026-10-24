package com.travel.ai_agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai_agent.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<String> chat(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestParam(value = "message", required = false) String queryMessage,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestBody(required = false) String bodyMessage) {

        String msg = (queryMessage != null && !queryMessage.isBlank()) ? queryMessage : bodyMessage;

        if ((msg == null || msg.isBlank()) && acceptHeader != null && acceptHeader.contains("text/html")) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
        }

        return handleChat(msg, conversationId);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, "*/*"})
    public ResponseEntity<String> chatPost(@RequestBody(required = false) String payload) {
        return handleChat(payload, null);
    }

    private ResponseEntity<String> handleChat(String rawInput, String conversationId) {
        if (rawInput == null || rawInput.isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }

        String message = rawInput.trim();
        String convId = (conversationId == null || conversationId.isBlank()) ? "default" : conversationId;

        if (message.startsWith("{") && message.endsWith("}")) {
            try {
                JsonNode jsonNode = objectMapper.readTree(message);
                if (jsonNode.has("conversationId") && !jsonNode.get("conversationId").isNull()) {
                    convId = jsonNode.get("conversationId").asText();
                }
                if (jsonNode.has("message") && !jsonNode.get("message").isNull()) {
                    message = jsonNode.get("message").asText();
                } else if (jsonNode.has("query") && !jsonNode.get("query").isNull()) {
                    message = jsonNode.get("query").asText();
                } else if (jsonNode.has("prompt") && !jsonNode.get("prompt").isNull()) {
                    message = jsonNode.get("prompt").asText();
                }
            } catch (Exception ignored) {
                // Fall back to original message string
            }
        }

        if (message.isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }

        try {
            String response = chatService.chat(message, convId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing chat: " + (e.getMessage() != null ? e.getMessage() : "Unexpected error"));
        }
    }
}