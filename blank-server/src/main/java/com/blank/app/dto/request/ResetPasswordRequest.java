package com.blank.app.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "请输入验证码")
    private String verifyCode;
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, message = "密码至少6位")
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
