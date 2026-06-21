package com.blank.app.dto.response;

import java.util.Map;

public class LoginResponse {
    private String token;
    private Map<String, Object> userInfo;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Map<String, Object> getUserInfo() { return userInfo; }
    public void setUserInfo(Map<String, Object> userInfo) { this.userInfo = userInfo; }
}
