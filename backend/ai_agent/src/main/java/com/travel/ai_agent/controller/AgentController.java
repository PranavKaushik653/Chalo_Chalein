package com.travel.ai_agent.controller;

import com.travel.ai_agent.dto.TripSyncItem;
import com.travel.ai_agent.repository.TripRepository;
import com.travel.ai_agent.service.ChatService;
import com.travel.ai_agent.service.TripSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ChatService chatService;
    private final TripSyncService tripSyncService;
    private final TripRepository tripRepository;

    public record ChatRequest(String message, String conversationId) {}
    public record ChatReply(String reply) {}

    /** The website sends its current trips here so the agent works on exactly what the traveller sees. */
    @PutMapping("/trips/sync")
    public Map<String, Object> sync(@RequestBody List<TripSyncItem> trips) {
        TripSyncService.Result r = tripSyncService.replaceAll(trips);
        return Map.of("synced", r.synced(), "skipped", r.skipped());
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "trips", tripRepository.count());
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest req) {
        if (req == null || req.message() == null || req.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }
        String message = req.message().trim();
        if (message.length() > 1000) message = message.substring(0, 1000);
        String conversationId = (req.conversationId() == null || req.conversationId().isBlank())
                ? "default" : req.conversationId();

        try {
            return ResponseEntity.ok(new ChatReply(chatService.chat(message, conversationId)));
        } catch (Exception e) {
            log.error("Chat failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "The travel agent is busy right now. Please try again in a moment."));
        }
    }
}