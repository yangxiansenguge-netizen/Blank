package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/polish")
    public ApiResponse<Map<String, Object>> polishText(@CurrentUser JwtUserDetails user,
                                                        @RequestBody Map<String, String> body) {
        return ApiResponse.success(aiService.polishText(user.getId(), body.get("text")));
    }

    @PostMapping("/generate-from-image")
    public ApiResponse<Map<String, Object>> generateFromImage(@CurrentUser JwtUserDetails user,
                                                               @RequestBody Map<String, String> body) {
        return ApiResponse.success(aiService.generateFromImage(user.getId(), body.get("imageUrl")));
    }

    @PostMapping("/custom")
    public ApiResponse<Map<String, Object>> customAi(@CurrentUser JwtUserDetails user,
                                                      @RequestBody Map<String, String> body) {
        return ApiResponse.success(aiService.customAi(user.getId(), body.get("text"), body.get("requirement")));
    }
}
