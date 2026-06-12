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

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/ask")
    public String askQuestion(@RequestBody String question) {

        String lowerQuestion = question.toLowerCase();

        if (!isCatQuestion(lowerQuestion)) {
            return "🚫 I am CatGPT. I only answer cat-related questions.";
        }

        try {

            String prompt = """
                    You are CatGPT.

                    You ONLY answer questions related to cats.

                    Allowed topics:
                    - cat behavior
                    - cat sounds
                    - cat food
                    - cat medicines
                    - cat grooming
                    - cat shopping
                    - cat breeds
                    - cat health
                    - vet guidance

                    Keep answers short and useful.

                    User Question:
                    """ + question;

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                            + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map responseBody = response.getBody();

            List candidates =
                    (List) responseBody.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return getFallbackAnswer(lowerQuestion);
            }

            Map candidate =
                    (Map) candidates.get(0);

            Map content =
                    (Map) candidate.get("content");

            List parts =
                    (List) content.get("parts");

            Map firstPart =
                    (Map) parts.get(0);

            return firstPart.get("text").toString();

        } catch (Exception e) {

            // Uncomment next line if you want to see real error in terminal
            // e.printStackTrace();

            return getFallbackAnswer(lowerQuestion);
        }
    }

    private boolean isCatQuestion(String question) {

        String[] keywords = {
                "cat",
                "cats",
                "kitten",
                "kittens",
                "feline",
                "meow",
                "purr",
                "tail",
                "paw",
                "whiskers",
                "fur",
                "litter",
                "breed",
                "grooming",
                "cat food",
                "vet",
                "medicine",
                "sleep",
                "bite",
                "scratch",
                "colour",
                "color"
        };

        for (String keyword : keywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String getFallbackAnswer(String question) {

        if (question.contains("sleep")) {
            return "🐱 Cats usually sleep 12–16 hours a day. This is normal unless your cat seems sick or stops eating.";
        }

        if (question.contains("meow")) {
            return "🐱 Cats meow to communicate with humans. Hunger, attention, stress, or affection can be reasons.";
        }

        if (question.contains("food")) {
            return "🐱 Cats need protein-rich food. Commercial cat food, chicken, and fish are common choices.";
        }

        if (question.contains("colour") || question.contains("color")) {
            return "🐱 Common cat colours include black, white, grey, ginger, orange, brown, and mixed patterns.";
        }

        if (question.contains("breed")) {
            return "🐱 Popular cat breeds include Persian, Maine Coon, Siamese, Ragdoll, and British Shorthair.";
        }

        if (question.contains("medicine")) {
            return "🐱 Always consult a veterinarian before giving medicine to your cat.";
        }

        return "🐱 I understand your cat question. Please try again later if you want an AI-generated answer.";
    }
}