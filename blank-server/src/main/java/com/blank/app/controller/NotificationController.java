package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(notificationService.list(user.getId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(notificationService.unreadCount(user.getId()));
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> readAll(@CurrentUser JwtUserDetails user) {
        notificationService.readAll(user.getId());
        return ApiResponse.success(null, "已全部标记为已读");
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@CurrentUser JwtUserDetails user, @PathVariable Integer id) {
        notificationService.markRead(user.getId(), id);
        return ApiResponse.success(null, "已标记为已读");
    }
}
