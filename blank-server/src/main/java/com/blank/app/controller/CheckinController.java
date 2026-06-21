package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(checkinService.getStatus(user.getId()));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> doCheckin(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(checkinService.doCheckin(user.getId()), "签到成功");
    }

    @PostMapping("/postcard-task-reward")
    public ApiResponse<Map<String, Object>> claimPostcardTaskReward(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(checkinService.claimPostcardTaskReward(user.getId()), "奖励领取成功");
    }
}
