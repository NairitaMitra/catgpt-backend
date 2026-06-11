package com.catgpt.catgpt_backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RestController
@RequestMapping("/api/cat")
@CrossOrigin("*")
public class CatController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create();

    @PostMapping("/ask")
    public String askQuestion(@RequestBody String question) {

        if (!isCatQuestion(question.toLowerCase())) {
            return "🚫 I am CatGPT. I only answer cat-related questions.";
        }

        try {

            String prompt = """
                    You are CatGPT.

                    You ONLY answer questions related to cats.

                    Topics allowed:
                    - cat behavior
                    - cat sounds
                    - cat health
                    - cat medicines
                    - food
                    - shopping
                    - vet guidance
                    - breeds

                    If unrelated to cats, refuse.

                    User Question:
                    """ + question;

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                            + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            Map response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            var candidates = (java.util.List<Map>) response.get("candidates");
            var content = (Map) candidates.get(0).get("content");
            var parts = (java.util.List<Map>) content.get("parts");

            return parts.get(0).get("text").toString();

        } catch (Exception e) {
            return "Something went wrong: " + e.getMessage();
        }
    }

    private boolean isCatQuestion(String question) {

        String[] keywords = {
                "cat", "kitten", "meow",
                "pet", "fur", "litter",
                "vet", "cat food",
                "breed", "medicine",
                "scratch", "bite"
        };

        for (String keyword : keywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}