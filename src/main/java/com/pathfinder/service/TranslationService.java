package com.pathfinder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    // Translation engines mis-transliterate these Bulgarian proper nouns; correct them post-translation.
    private static final java.util.Map<String, String> PROPER_NOUN_FIXES = java.util.Map.of(
            "Kalaka", "Kaylaka",
            "kalaka", "kaylaka"
    );

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final String libreTranslateUrl;

    public TranslationService(ObjectMapper objectMapper,
                               @Value("${pathfinder.libretranslate-url:http://localhost:5000}") String libreTranslateUrl) {
        this.objectMapper = objectMapper;
        this.libreTranslateUrl = libreTranslateUrl;
    }

    /**
     * Translates text from sourceLang to targetLang.
     * Primary: Google Translate unofficial endpoint (no key, no Docker, fast).
     * Fallback 1: LibreTranslate (self-hosted Docker).
     * Fallback 2: MyMemory free API.
     * Returns original text unchanged if all providers fail.
     */
    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) return text;
        String key = targetLang + ":" + text;
        String cached = cache.get(key);
        if (cached != null) return cached;

        // Primary: Google Translate unofficial (no API key required)
        try {
            String result = applyProperNounFixes(translateWithGoogle(text, sourceLang, targetLang));
            cache.put(key, result);
            return result;
        } catch (Exception e) {
            log.warn("Google Translate failed ({}→{}): {} — trying LibreTranslate", sourceLang, targetLang, e.getMessage());
        }

        // Fallback 1: LibreTranslate (requires Docker container)
        try {
            String result = applyProperNounFixes(translateWithLibre(text, sourceLang, targetLang));
            cache.put(key, result);
            return result;
        } catch (Exception e) {
            log.warn("LibreTranslate failed ({}→{}): {} — trying MyMemory", sourceLang, targetLang, e.getMessage());
        }

        // Fallback 2: MyMemory
        String result = applyProperNounFixes(translateWithMyMemoryFallback(text, sourceLang, targetLang));
        if (!result.equals(text)) cache.put(key, result);
        return result;
    }

    /**
     * Translates text to Bulgarian, skipping if the text is already predominantly Cyrillic.
     */
    public String translateToBulgarian(String text) {
        if (text == null || text.isBlank()) return text;
        if (isCyrillic(text)) return text;
        return translate(text, "en", "bg");
    }

    // ── Google Translate (unofficial, no API key) ─────────────────────────────

    private String translateWithGoogle(String text, String sourceLang, String targetLang)
            throws IOException, InterruptedException {
        // Splits at sentence boundaries to stay within Google's ~5000 char soft limit.
        if (text.length() <= 4500) {
            return translateChunkGoogle(text, sourceLang, targetLang);
        }
        StringBuilder sb = new StringBuilder();
        for (String chunk : splitIntoChunks(text, 4000)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(translateChunkGoogle(chunk, sourceLang, targetLang));
        }
        return sb.toString();
    }

    private String translateChunkGoogle(String text, String sourceLang, String targetLang)
            throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://translate.googleapis.com/translate_a/single"
                + "?client=gtx&sl=" + sourceLang + "&tl=" + targetLang
                + "&dt=t&q=" + encoded;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Google Translate returned HTTP " + response.statusCode());
        }

        // Response shape: [[[translatedText, originalText, ...], ...], ...]
        JsonNode root = objectMapper.readTree(response.body());
        StringBuilder result = new StringBuilder();
        JsonNode segments = root.get(0);
        if (segments == null || !segments.isArray()) {
            throw new IOException("Unexpected Google Translate response structure");
        }
        for (JsonNode segment : segments) {
            JsonNode part = segment.get(0);
            if (part != null && !part.isNull()) {
                result.append(part.asText());
            }
        }
        String translated = result.toString().trim();
        if (translated.isBlank()) throw new IOException("Empty response from Google Translate");
        return translated;
    }

    // ── LibreTranslate (self-hosted Docker fallback) ──────────────────────────

    private String translateWithLibre(String text, String sourceLang, String targetLang)
            throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("q", text, "source", sourceLang, "target", targetLang, "format", "text")
        );

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(libreTranslateUrl + "/translate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("LibreTranslate returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String translated = root.path("translatedText").asText();
        if (translated.isBlank()) throw new IOException("Empty translatedText in LibreTranslate response");
        return translated;
    }

    // ── MyMemory (free API, 500-char limit per request) ───────────────────────

    private String translateWithMyMemoryFallback(String text, String sourceLang, String targetLang) {
        try {
            if (text.length() <= 500) {
                return translateChunkMyMemory(text, sourceLang, targetLang);
            }
            StringBuilder result = new StringBuilder();
            for (String chunk : splitIntoChunks(text, 450)) {
                if (!result.isEmpty()) result.append(" ");
                result.append(translateChunkMyMemory(chunk, sourceLang, targetLang));
            }
            return result.toString();
        } catch (Exception e) {
            log.warn("MyMemory fallback also failed ({}→{}): {}", sourceLang, targetLang, e.getMessage());
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return text;
        }
    }

    private String translateChunkMyMemory(String chunk, String sourceLang, String targetLang)
            throws IOException, InterruptedException {
        String encoded  = URLEncoder.encode(chunk, StandardCharsets.UTF_8);
        String langpair = URLEncoder.encode(sourceLang + "|" + targetLang, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langpair;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("MyMemory returned {} for {}→{}", response.statusCode(), sourceLang, targetLang);
            return chunk;
        }

        JsonNode root = objectMapper.readTree(response.body());
        String translated = root.path("responseData").path("translatedText").asText();
        return translated.isBlank() ? chunk : translated;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String applyProperNounFixes(String text) {
        for (var entry : PROPER_NOUN_FIXES.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private boolean isCyrillic(String text) {
        long cyrillic = text.chars().filter(c -> c >= 0x0400 && c <= 0x04FF).count();
        long letters  = text.chars().filter(Character::isLetter).count();
        return letters > 0 && (double) cyrillic / letters > 0.5;
    }

    private java.util.List<String> splitIntoChunks(String text, int maxChunk) {
        if (text.length() <= maxChunk) return java.util.List.of(text);

        java.util.List<String> chunks = new java.util.ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() + sentence.length() + 1 > maxChunk && !current.isEmpty()) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            if (sentence.length() > maxChunk) {
                if (!current.isEmpty()) {
                    chunks.add(current.toString().trim());
                    current.setLength(0);
                }
                chunks.add(sentence.substring(0, maxChunk));
            } else {
                if (!current.isEmpty()) current.append(" ");
                current.append(sentence);
            }
        }
        if (!current.isEmpty()) chunks.add(current.toString().trim());
        return chunks;
    }
}
