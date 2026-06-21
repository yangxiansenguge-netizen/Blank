package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getFriends(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(friendService.getFriends(user.getId()));
    }

    @GetMapping("/pending")
    public ApiResponse<List<Map<String, Object>>> getPendingRequests(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(friendService.getPendingRequests(user.getId()));
    }

    @PostMapping("/request")
    public ApiResponse<Void> sendRequest(@CurrentUser JwtUserDetails user,
                                          @RequestBody Map<String, Object> body) {
        Integer targetUserId = (Integer) body.get("userId");
        friendService.sendRequest(user.getId(), targetUserId);
        return ApiResponse.success(null, "好友请求已发送");
    }

    @PutMapping("/{id}/accept")
    public ApiResponse<Void> acceptRequest(@CurrentUser JwtUserDetails user,
                                            @PathVariable Integer id) {
        friendService.acceptRequest(user.getId(), id);
        return ApiResponse.success(null, "已接受好友请求");
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> rejectRequest(@CurrentUser JwtUserDetails user,
                                            @PathVariable Integer id) {
        friendService.rejectRequest(user.getId(), id);
        return ApiResponse.success(null, "已拒绝好友请求");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFriend(@CurrentUser JwtUserDetails user,
                                           @PathVariable Integer id) {
        friendService.deleteFriend(user.getId(), id);
        return ApiResponse.success(null, "好友已删除");
    }
}
