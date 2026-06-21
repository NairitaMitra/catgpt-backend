package com.catgpt.catgpt_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatControllerTest {

    private CatController controller;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        controller = new CatController();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(controller, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);
    }

    @Test
    void askQuestionReturnsPromptForBlankInput() {
        assertThat(controller.askQuestion("   ")).isEqualTo("🐱 Please ask me something about cats.");
    }

    @Test
    void askQuestionReturnsFallbackWhenApiCallFails() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("boom"));

        String response = controller.askQuestion("Are cats mammals?");

        assertThat(response).isEqualTo("🚫 I am CatGPT. I only answer cat-related questions.");
    }

    @Test
    void askQuestionReturnsResponseContentForSuccessfulApiCall() {
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "Cats love naps."))
                )
        );

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String response = controller.askQuestion("Why do cats sleep so much?");

        assertThat(response).isEqualTo("Cats love naps.");
    }

    @Test
    void askQuestionReturnsFallbackMessageWhenChoicesMissing() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of()));

        String response = controller.askQuestion("cat sleep");

        assertThat(response).isEqualTo("🐱 Cats usually sleep 12–16 hours a day. This is normal unless your cat seems weak or stops eating.");
    }
}
