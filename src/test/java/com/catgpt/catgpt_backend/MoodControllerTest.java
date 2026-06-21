package com.catgpt.catgpt_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MoodControllerTest {

    private MoodController controller;
    private OpenRouterVisionService visionService;

    @BeforeEach
    void setUp() {
        visionService = mock(OpenRouterVisionService.class);
        controller = new MoodController(visionService);
    }

    @Test
    void detectMoodReturnsPromptWhenFileIsMissing() {
        assertThat(controller.detectMood(null))
                .isEqualTo("🐾 Please upload a cat photo.");
    }

    @Test
    void detectMoodReturnsPromptForEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", "image/png", new byte[0]
        );

        assertThat(controller.detectMood(file))
                .isEqualTo("🐾 Please upload a cat photo.");
    }

    @Test
    void detectMoodReturnsPromptForNonImageFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "note.txt", "text/plain", "hello".getBytes()
        );

        assertThat(controller.detectMood(file))
                .isEqualTo("🐾 Please upload a valid image file.");
    }

    @Test
    void detectMoodReturnsPromptWhenFileIsTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", new byte[6_000_000]
        );

        assertThat(controller.detectMood(file))
                .isEqualTo("🐾 Image size must be under 5 MB.");
    }

    @Test
    void detectMoodReturnsServiceResultForValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "image-bytes".getBytes()
        );
        when(visionService.analyzeCatMood(any())).thenReturn("🐱 Mood: Happy");

        assertThat(controller.detectMood(file))
                .isEqualTo("🐱 Mood: Happy");
    }

    @Test
    void detectMoodReturnsBusyMessageWhenRateLimited() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "image-bytes".getBytes()
        );
        when(visionService.analyzeCatMood(any())).thenThrow(new RuntimeException("429 Too Many Requests"));

        assertThat(controller.detectMood(file))
                .isEqualTo("🐾 CatGPT Vision is busy right now. Please try again later.");
    }

    @Test
    void detectMoodReturnsGenericErrorMessageForOtherExceptions() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.png", "image/png", "image-bytes".getBytes()
        );
        when(visionService.analyzeCatMood(any())).thenThrow(new RuntimeException("server down"));

        assertThat(controller.detectMood(file))
                .startsWith("🐾 CatGPT could not analyze this image right now. Please try again.");
    }
}
