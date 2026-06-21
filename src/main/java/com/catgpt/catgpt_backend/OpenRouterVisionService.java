package com.catgpt.catgpt_backend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterVisionService {

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeCatMood(MultipartFile file) throws Exception {

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        String imageDataUrl =
                "data:" + file.getContentType() + ";base64," + base64Image;

        String prompt = """
                You are CatGPT, an AI assistant only for cats.

                Analyze the uploaded image.

                Step 1: Determine whether the image contains a REAL DOMESTIC HOUSE CAT (Felis catus).

                Accept:
                - Domestic pet cats
                - Stray house cats
                - Kittens

                Reject:
                - Lions
                - Tigers
                - Leopards
                - Cheetahs
                - Jaguars
                - Pumas
                - Lynx
                - Other wild cats
                - Dogs
                - Humans
                - Birds
                - Other animals
                - Cartoons
                - Drawings
                - Paintings
                - Toys
                - Statues
                - AI-generated images

                If the image is NOT a real domestic house cat, DO NOT analyze mood.

                Instead reply ONLY in this format:

                🐾 CatGPT Verdict: Not a Cat

                Detected Subject: <what is actually visible in the image>

                Reason: CatGPT only analyzes real domestic house cats.

                Examples:
                - Detected Subject: Baby Lion
                - Detected Subject: Tiger
                - Detected Subject: Human
                - Detected Subject: Dog
                - Detected Subject: Cartoon Cat
                - Detected Subject: AI Generated Cat Artwork

                Please upload a photo of a real domestic house cat.

                If confidence is below 95%, reject the image.

                If the image contains a real domestic house cat, continue with mood analysis.

                Return only this format:

                🐱 Mood: <Happy / Relaxed / Curious / Playful / Sleepy / Angry / Scared>

                Confidence: <percentage>

                Reason:
                <short reason based on eyes, ears, posture or expression>

                CatGPT Recommendation:
                <short helpful suggestion>

                """;

        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);
        headers.set("HTTP-Referer", "https://catgpt-by-nairita.netlify.app");
        headers.set("X-Title", "CatGPT by Nairita");

        Map<String, Object> requestBody = Map.of(
                "model", "nvidia/nemotron-nano-12b-v2-vl:free",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", prompt
                                        ),
                                        Map.of(
                                                "type", "image_url",
                                                "image_url", Map.of(
                                                        "url", imageDataUrl
                                                )
                                        )
                                )
                        )
                ),
                "max_tokens", 300,
                "temperature", 0.2
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        Map body = response.getBody();

        if (body == null || body.get("choices") == null) {
            return "🐾 CatGPT could not analyze this image right now.";
        }

        List choices = (List) body.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        return message.get("content").toString();
    }
}
