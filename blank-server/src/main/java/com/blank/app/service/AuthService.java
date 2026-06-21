package com.blank.app.service;

import com.blank.app.dto.request.*;
import com.blank.app.dto.response.LoginResponse;

public interface AuthService {
    void sendVerifyCode(SendVerifyCodeRequest req);
    LoginResponse register(RegisterRequest req);
    LoginResponse login(LoginRequest req);
    void logout(String bearerToken);
    void forgotPassword(ForgotPasswordRequest req);
    void resetPassword(ResetPasswordRequest req);
}
