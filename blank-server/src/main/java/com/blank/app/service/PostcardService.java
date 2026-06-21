package com.blank.app.service;

import com.blank.app.dto.request.BatchDeleteRequest;
import com.blank.app.dto.request.CreatePostcardRequest;
import com.blank.app.dto.request.DriftElementRequest;
import com.blank.app.dto.response.PageResponse;

import java.util.List;
import java.util.Map;

public interface PostcardService {
    Map<String, Object> getDiscover(Integer page, Integer pageSize, Integer userId);
    Map<String, Object> getDrifting(Integer page, Integer pageSize, Integer userId);
    Map<String, Object> getDetail(Integer id, Integer userId);
    Map<String, Object> create(Integer userId, CreatePostcardRequest req);
    void deletePostcard(Integer userId, Integer postcardId);
    void batchDelete(Integer userId, BatchDeleteRequest req);
    Map<String, Object> getInbox(Integer userId, Integer page, Integer pageSize);
    Map<String, Object> getOutbox(Integer userId, Integer page, Integer pageSize);
    Map<String, Object> getFavorites(Integer userId, Integer page, Integer pageSize);
    Map<String, Object> toggleLike(Integer userId, Integer postcardId);
    Map<String, String> uploadImage(org.springframework.web.multipart.MultipartFile file);
    Map<String, Object> addDriftElement(Integer postcardId, Integer userId, String userName, Map<String, Object> element);
    Map<String, Object> deleteDriftElement(Integer postcardId, Integer idx, Integer userId);
    Map<String, Object> updateDriftElements(Integer postcardId, Integer userId, String userName, List<Map<String, Object>> elements);
}
