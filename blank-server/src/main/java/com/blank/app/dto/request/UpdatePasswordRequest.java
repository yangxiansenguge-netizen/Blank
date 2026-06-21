package com.blank.app.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdatePasswordRequest {
    @NotBlank(message = "请输入旧密码")
    private String oldPassword;
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, message = "密码至少6位")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
