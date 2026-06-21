package com.blank.app.service;

import com.blank.app.dto.request.DeleteAccountRequest;
import com.blank.app.dto.request.UpdatePasswordRequest;
import com.blank.app.dto.request.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> getProfile(Integer userId);
    Map<String, Object> updateProfile(Integer userId, UpdateProfileRequest req);
    Map<String, String> updateAvatar(Integer userId, MultipartFile file);
    void updatePassword(Integer userId, UpdatePasswordRequest req);
    void deleteAccount(Integer userId, DeleteAccountRequest req);
    Map<String, Object> getStats(Integer userId);
    List<Map<String, Object>> searchUser(Integer userId, String keyword);
}
