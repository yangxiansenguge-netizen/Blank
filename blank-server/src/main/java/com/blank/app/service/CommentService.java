package com.blank.app.service;

import java.util.List;
import java.util.Map;

public interface CommentService {
    List<Map<String, Object>> getComments(Integer postcardId, Integer userId);
    Map<String, Object> addComment(Integer postcardId, Integer userId, String content);
    void deleteComment(Integer commentId, Integer userId);
    Map<String, Object> togglePin(Integer commentId, Integer userId);
    Map<String, Object> likeComment(Integer commentId, Integer userId);
}
