package com.blank.app.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "请输入用户名")
    private String username;
    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "请输入密码")
    @Size(min = 6, message = "密码至少6位")
    private String password;
    @NotBlank(message = "请输入验证码")
    private String verifyCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
}
