package com.blank.app.service.impl;

import com.blank.app.entity.Notification;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.NotificationMapper;
import com.blank.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationMapper notificationMapper;

    @Override
    public List<Map<String, Object>> list(Integer userId) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Notification n : notificationMapper.selectByUserId(userId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId()); item.put("type", n.getType()); item.put("title", n.getTitle());
            item.put("content", n.getContent()); item.put("postcardId", n.getPostcardId());
            item.put("isRead", n.getIsRead() == 1); item.put("createdAt", n.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override
    public Map<String, Long> unreadCount(Integer userId) {
        Map<String, Long> r = new HashMap<>();
        r.put("count", notificationMapper.countUnread(userId)); return r;
    }

    @Override
    public void readAll(Integer userId) { notificationMapper.markAllRead(userId); }

    @Override
    public void markRead(Integer userId, Integer nid) {
        Notification n = notificationMapper.selectById(nid);
        if (n == null) throw new BusinessException("通知不存在", 404);
        if (!n.getUserId().equals(userId)) throw new BusinessException("无权操作", 403);
        n.setIsRead(1); notificationMapper.updateById(n);
    }
}
