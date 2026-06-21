package com.blank.app.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class SendVerifyCodeRequest {
    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
