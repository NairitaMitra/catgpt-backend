package com.catgpt.catgpt_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cat")
@CrossOrigin("*")
public class CatController {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ask")
    public String askQuestion(@RequestBody String question) {

        if (question == null || question.trim().isEmpty()) {
            return "🐱 Please ask me something about cats.";
        }

        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.1-8b-instant",
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", """
                                            You are CatGPT.

                                            You ONLY answer cat-related questions.

                                            Allowed topics:
                                            - cat behavior
                                            - cat sounds
                                            - cat food
                                            - cat medicine
                                            - cat shopping
                                            - cat breeds
                                            - cat grooming
                                            - vet guidance

                                            If the user asks anything unrelated to cats, reply exactly:
                                            🚫 I am CatGPT. I only answer cat-related questions.

                                            Keep answers short, friendly, and useful.
                                            For serious health issues, suggest contacting a vet.
                                            """
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", question
                            )
                    ),
                    "temperature", 0.5,
                    "max_tokens", 250
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map body = response.getBody();
            List choices = (List) body.get("choices");

            if (choices == null || choices.isEmpty()) {
                return fallback(question);
            }

            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            return fallback(question);
        }
    }

    private String fallback(String question) {
        String q = question.toLowerCase();

        if (!q.contains("cat") && !q.contains("kitten") && !q.contains("meow")
                && !q.contains("feline") && !q.contains("paw")) {
            return "🚫 I am CatGPT. I only answer cat-related questions.";
        }

        if (q.contains("sleep")) {
            return "🐱 Cats usually sleep 12–16 hours a day. This is normal unless your cat seems weak or stops eating.";
        }

        if (q.contains("meow")) {
            return "🐱 Cats meow for attention, hunger, stress, greeting, or communication.";
        }

        if (q.contains("food")) {
            return "🍗 Cats need protein-rich food. Balanced cat food is usually safest.";
        }

        if (q.contains("medicine")) {
            return "💊 Never give human medicine to cats. Please consult a vet.";
        }

        return "🐱 I understand your cat question. Please try again in a moment.";
    }
}