package com.blank.app.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getOverview();
    Map<String, Object> getPostcards(int page, int pageSize, String keyword, String postcardType, String status);
    Map<String, Object> createPostcard(Map<String, Object> body);
    void updatePostcard(Integer id, Map<String, Object> body);
    void deletePostcard(Integer id);
    List<Map<String, Object>> getStampSeries();
    void createStampSeries(Map<String, Object> body);
    void updateStampSeries(Integer id, Map<String, Object> body);
    void deleteStampSeries(Integer id);
    List<Map<String, Object>> getStamps(String keyword, String seriesId);
    void createStamp(Map<String, Object> body);
    void updateStamp(Integer id, Map<String, Object> body);
    void deleteStamp(Integer id);
    Map<String, String> uploadStampImage(MultipartFile file, String seriesName);
    List<Map<String, Object>> getActivationCodes();
    Map<String, Object> generateActivationCodes(Integer userId, Map<String, Object> body);
    void deleteActivationCode(Integer id);
    List<Map<String, Object>> getPendingPostcards();
    void approvePostcard(Integer id);
    void rejectPostcard(Integer id, String reason);
}
