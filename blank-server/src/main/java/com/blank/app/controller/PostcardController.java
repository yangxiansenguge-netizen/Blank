package com.blank.app.controller;

import com.blank.app.dto.request.BatchDeleteRequest;
import com.blank.app.dto.request.CreatePostcardRequest;
import com.blank.app.dto.request.DriftElementRequest;
import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.PostcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/postcards")
public class PostcardController {

    @Autowired
    private PostcardService postcardService;

    @GetMapping("/discover")
    public ApiResponse<Map<String, Object>> getDiscover(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @CurrentUser(required = false) JwtUserDetails user) {
        Integer userId = user != null ? user.getId() : null;
        Map<String, Object> data = postcardService.getDiscover(page, pageSize, userId);
        return new ApiResponse<>(0, "success", data);
    }

    @GetMapping("/drifting")
    public ApiResponse<Map<String, Object>> getDrifting(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @CurrentUser(required = false) JwtUserDetails user) {
        Integer userId = user != null ? user.getId() : null;
        Map<String, Object> data = postcardService.getDrifting(page, pageSize, userId);
        return new ApiResponse<>(0, "success", data);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> getDetail(
            @PathVariable Integer id,
            @CurrentUser(required = false) JwtUserDetails user) {
        Integer userId = user != null ? user.getId() : null;
        return ApiResponse.success(postcardService.getDetail(id, userId));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@CurrentUser JwtUserDetails user,
                                                    @Valid @RequestBody CreatePostcardRequest req) {
        Map<String, Object> result = postcardService.create(user.getId(), req);
        boolean isScheduled = "scheduled".equals(result.get("status"));
        return ApiResponse.success(result, isScheduled ? "明信片已预约发送" : "明信片发送成功，正在审核中");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePostcard(@CurrentUser JwtUserDetails user,
                                             @PathVariable Integer id) {
        postcardService.deletePostcard(user.getId(), id);
        return ApiResponse.success(null, "删除成功");
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@CurrentUser JwtUserDetails user,
                                          @RequestBody BatchDeleteRequest req) {
        postcardService.batchDelete(user.getId(), req);
        return ApiResponse.success(null, "批量删除成功");
    }

    @GetMapping("/my/inbox")
    public ApiResponse<Map<String, Object>> getInbox(
            @CurrentUser JwtUserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return new ApiResponse<>(0, "success", postcardService.getInbox(user.getId(), page, pageSize));
    }

    @GetMapping("/my/outbox")
    public ApiResponse<Map<String, Object>> getOutbox(
            @CurrentUser JwtUserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return new ApiResponse<>(0, "success", postcardService.getOutbox(user.getId(), page, pageSize));
    }

    @GetMapping("/my/favorites")
    public ApiResponse<Map<String, Object>> getFavorites(
            @CurrentUser JwtUserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return new ApiResponse<>(0, "success", postcardService.getFavorites(user.getId(), page, pageSize));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@CurrentUser JwtUserDetails user,
                                                        @PathVariable Integer id) {
        return ApiResponse.success(postcardService.toggleLike(user.getId(), id));
    }

    @PostMapping("/upload-image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        return ApiResponse.success(postcardService.uploadImage(file), "上传成功");
    }

    @PutMapping("/{id}/drift-element")
    public ApiResponse<Map<String, Object>> addDriftElement(
            @CurrentUser JwtUserDetails user,
            @PathVariable Integer id,
            @Valid @RequestBody DriftElementRequest req) {
        return ApiResponse.success(postcardService.addDriftElement(id, user.getId(), user.getUsername(), req.getElement()));
    }

    @DeleteMapping("/{id}/drift-element/{idx}")
    public ApiResponse<Map<String, Object>> deleteDriftElement(
            @CurrentUser JwtUserDetails user,
            @PathVariable Integer id,
            @PathVariable Integer idx) {
        return ApiResponse.success(postcardService.deleteDriftElement(id, idx, user.getId()));
    }

    @PutMapping("/{id}/drift-elements")
    public ApiResponse<Map<String, Object>> updateDriftElements(
            @CurrentUser JwtUserDetails user,
            @PathVariable Integer id,
            @RequestBody List<Map<String, Object>> elements) {
        return ApiResponse.success(postcardService.updateDriftElements(id, user.getId(), user.getUsername(), elements));
    }
}
