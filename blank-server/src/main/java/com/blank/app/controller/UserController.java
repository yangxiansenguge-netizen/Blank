package com.blank.app.controller;

import com.blank.app.dto.request.DeleteAccountRequest;
import com.blank.app.dto.request.UpdatePasswordRequest;
import com.blank.app.dto.request.UpdateProfileRequest;
import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(userService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@CurrentUser JwtUserDetails user,
                                                           @RequestBody UpdateProfileRequest req) {
        return ApiResponse.success(userService.updateProfile(user.getId(), req), "资料更新成功");
    }

    @PutMapping("/avatar")
    public ApiResponse<Map<String, String>> updateAvatar(@CurrentUser JwtUserDetails user,
                                                          @RequestParam("avatar") MultipartFile file) {
        return ApiResponse.success(userService.updateAvatar(user.getId(), file), "头像更新成功");
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@CurrentUser JwtUserDetails user,
                                             @Valid @RequestBody UpdatePasswordRequest req) {
        userService.updatePassword(user.getId(), req);
        return ApiResponse.success(null, "密码修改成功");
    }

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(@CurrentUser JwtUserDetails user,
                                            @Valid @RequestBody DeleteAccountRequest req) {
        userService.deleteAccount(user.getId(), req);
        return ApiResponse.success(null, "账号已删除");
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(userService.getStats(user.getId()));
    }

    @GetMapping("/search")
    public ApiResponse<List<Map<String, Object>>> searchUser(@CurrentUser JwtUserDetails user,
                                                              @RequestParam String keyword) {
        return ApiResponse.success(userService.searchUser(user.getId(), keyword));
    }
}
