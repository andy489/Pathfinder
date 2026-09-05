package com.pathfinder.web.rest;

import com.pathfinder.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslateRestController {

    private final TranslationService translationService;

    public TranslateRestController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> translate(@RequestBody Map<String, String> body) {
        String text   = body.get("q");
        String target = body.get("target");

        if (text == null || text.isBlank() || target == null || target.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing q or target"));
        }

        String translated = translationService.translate(text, "bg", target);
        return ResponseEntity.ok(Map.of("translatedText", translated));
    }
}
