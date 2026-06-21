package com.blank.app.dto.request;

import javax.validation.constraints.NotBlank;

public class DeleteAccountRequest {
    @NotBlank(message = "请输入密码")
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
