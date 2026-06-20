package com.catgpt.catgpt_backend;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cat")
@CrossOrigin("*")
public class MoodController {

    private final OpenRouterVisionService openRouterVisionService;

    public MoodController(OpenRouterVisionService openRouterVisionService) {
        this.openRouterVisionService = openRouterVisionService;
    }

    @PostMapping(
            value = "/mood",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String detectMood(@RequestParam("file") MultipartFile file) {

        try {
            if (file == null || file.isEmpty()) {
                return "🐾 Please upload a cat photo.";
            }

            if (file.getContentType() == null ||
                    !file.getContentType().startsWith("image")) {
                return "🐾 Please upload a valid image file.";
            }

            if (file.getSize() > 5_000_000) {
                return "🐾 Image size must be under 5 MB.";
            }

            return openRouterVisionService.analyzeCatMood(file);

        } catch (Exception e) {
            e.printStackTrace();

            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return "🐾 CatGPT Vision is busy right now. Please try again later.";
            }

            return "🐾 CatGPT could not analyze this image right now. Please try again."+e.getMessage();
        }
    }
}
