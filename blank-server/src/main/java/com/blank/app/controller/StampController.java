package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.StampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stamps")
public class StampController {

    @Autowired
    private StampService stampService;

    @GetMapping("/series")
    public ApiResponse<List<Map<String, Object>>> getStampSeries() {
        return ApiResponse.success(stampService.getStampSeries());
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getStamps(
            @RequestParam(required = false) String category,
            @CurrentUser(required = false) JwtUserDetails user) {
        Integer userId = user != null ? user.getId() : null;
        return ApiResponse.success(stampService.getStamps(category, userId));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyStamps(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(stampService.getMyStamps(user.getId()));
    }

    @PostMapping("/purchase")
    public ApiResponse<Map<String, Object>> purchaseStamp(@CurrentUser JwtUserDetails user,
                                                           @RequestBody Map<String, Integer> body) {
        Integer stampId = body.get("stampId");
        return ApiResponse.success(stampService.purchaseStamp(user.getId(), stampId), "购买成功");
    }
}
