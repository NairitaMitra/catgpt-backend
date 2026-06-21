package com.catgpt.catgpt_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
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

class OpenRouterVisionServiceTest {

    private OpenRouterVisionService service;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        service = new OpenRouterVisionService();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "openRouterApiKey", "test-key");
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    @Test
    void analyzeCatMoodReturnsResponseContentForSuccessfulCall() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "image-bytes".getBytes()
        );

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "🐱 Mood: Happy"))
                )
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = service.analyzeCatMood(file);

        assertThat(result).isEqualTo("🐱 Mood: Happy");
    }

    @Test
    void analyzeCatMoodReturnsFallbackWhenResponseBodyIsMissingChoices() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "image-bytes".getBytes()
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of()));

        String result = service.analyzeCatMood(file);

        assertThat(result).isEqualTo("🐾 CatGPT could not analyze this image right now.");
    }
}
