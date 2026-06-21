package com.blank.app.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    List<Map<String, Object>> list(Integer userId);
    Map<String, Long> unreadCount(Integer userId);
    void readAll(Integer userId);
    void markRead(Integer userId, Integer notificationId);
}
