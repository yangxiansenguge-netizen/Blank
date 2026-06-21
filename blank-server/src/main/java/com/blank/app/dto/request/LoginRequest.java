package com.blank.app.dto.request;

import javax.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "请输入邮箱")
    private String email;
    @NotBlank(message = "请输入密码")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
