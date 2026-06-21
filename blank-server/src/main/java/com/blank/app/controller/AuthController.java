package com.blank.app.controller;

import com.blank.app.dto.request.*;
import com.blank.app.dto.response.ApiResponse;
import com.blank.app.dto.response.LoginResponse;
import com.blank.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/verify")
    public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody SendVerifyCodeRequest req) {
        authService.sendVerifyCode(req);
        return ApiResponse.success(null, "验证码已发送");
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse resp = authService.register(req);
        return ApiResponse.success(resp, "注册成功");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ApiResponse.success(resp, "登录成功");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ApiResponse.success(null, "已退出登录");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ApiResponse.success(null, "验证码已发送");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.success(null, "密码重置成功");
    }
}
