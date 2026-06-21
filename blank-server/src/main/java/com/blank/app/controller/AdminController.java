package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        return ApiResponse.success(adminService.getOverview());
    }

    @GetMapping("/postcards")
    public ApiResponse<Map<String, Object>> getPostcards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String postcardType,
            @RequestParam(required = false) String status) {
        return new ApiResponse<>(0, "success", adminService.getPostcards(page, pageSize, keyword, postcardType, status));
    }

    @PostMapping("/postcards")
    public ApiResponse<Map<String, Object>> createPostcard(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.createPostcard(body), "明信片创建成功");
    }

    @PutMapping("/postcards/{id}")
    public ApiResponse<Void> updatePostcard(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        adminService.updatePostcard(id, body);
        return ApiResponse.success(null, "明信片更新成功");
    }

    @DeleteMapping("/postcards/{id}")
    public ApiResponse<Void> deletePostcard(@PathVariable Integer id) {
        adminService.deletePostcard(id);
        return ApiResponse.success(null, "明信片删除成功");
    }

    @GetMapping("/stamp-series")
    public ApiResponse<List<Map<String, Object>>> getStampSeries() {
        return ApiResponse.success(adminService.getStampSeries());
    }

    @PostMapping("/stamp-series")
    public ApiResponse<Void> createStampSeries(@RequestBody Map<String, Object> body) {
        adminService.createStampSeries(body);
        return ApiResponse.success(null, "邮票系列创建成功");
    }

    @PutMapping("/stamp-series/{id}")
    public ApiResponse<Void> updateStampSeries(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        adminService.updateStampSeries(id, body);
        return ApiResponse.success(null, "邮票系列更新成功");
    }

    @DeleteMapping("/stamp-series/{id}")
    public ApiResponse<Void> deleteStampSeries(@PathVariable Integer id) {
        adminService.deleteStampSeries(id);
        return ApiResponse.success(null, "邮票系列删除成功");
    }

    @GetMapping("/stamps")
    public ApiResponse<List<Map<String, Object>>> getStamps(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String seriesId) {
        return ApiResponse.success(adminService.getStamps(keyword, seriesId));
    }

    @PostMapping("/stamps")
    public ApiResponse<Void> createStamp(@RequestBody Map<String, Object> body) {
        adminService.createStamp(body);
        return ApiResponse.success(null, "邮票创建成功");
    }

    @PutMapping("/stamps/{id}")
    public ApiResponse<Void> updateStamp(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        adminService.updateStamp(id, body);
        return ApiResponse.success(null, "邮票更新成功");
    }

    @DeleteMapping("/stamps/{id}")
    public ApiResponse<Void> deleteStamp(@PathVariable Integer id) {
        adminService.deleteStamp(id);
        return ApiResponse.success(null, "邮票删除成功");
    }

    @PostMapping("/stamps/upload-image")
    public ApiResponse<Map<String, String>> uploadStampImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam(required = false, defaultValue = "") String seriesName) {
        return ApiResponse.success(adminService.uploadStampImage(file, seriesName), "上传成功");
    }

    @GetMapping("/activation-codes")
    public ApiResponse<List<Map<String, Object>>> getActivationCodes() {
        return ApiResponse.success(adminService.getActivationCodes());
    }

    @PostMapping("/activation-codes/generate")
    public ApiResponse<Map<String, Object>> generateActivationCodes(
            @CurrentUser JwtUserDetails user,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.generateActivationCodes(user.getId(), body),
                "激活码生成成功");
    }

    @DeleteMapping("/activation-codes/{id}")
    public ApiResponse<Void> deleteActivationCode(@PathVariable Integer id) {
        adminService.deleteActivationCode(id);
        return ApiResponse.success(null, "激活码删除成功");
    }

    @GetMapping("/audit/pending")
    public ApiResponse<List<Map<String, Object>>> getPendingPostcards() {
        return ApiResponse.success(adminService.getPendingPostcards());
    }

    @PostMapping("/audit/{id}/approve")
    public ApiResponse<Void> approvePostcard(@PathVariable Integer id) {
        adminService.approvePostcard(id);
        return ApiResponse.success(null, "审核通过");
    }

    @PostMapping("/audit/{id}/reject")
    public ApiResponse<Void> rejectPostcard(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "内容不符合社区规范");
        adminService.rejectPostcard(id, reason);
        return ApiResponse.success(null, "已驳回");
    }
}
