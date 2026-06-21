package com.blank.app.service;

import java.util.List;
import java.util.Map;

public interface FriendService {
    List<Map<String, Object>> getFriends(Integer userId);
    List<Map<String, Object>> getPendingRequests(Integer userId);
    void sendRequest(Integer userId, Integer targetUserId);
    void acceptRequest(Integer userId, Integer requestId);
    void rejectRequest(Integer userId, Integer requestId);
    void deleteFriend(Integer userId, Integer friendId);
}
