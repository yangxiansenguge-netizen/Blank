package com.blank.app.service.impl;

import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired private FriendMapper friendMapper;
    @Autowired private UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getFriends(Integer userId) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Friend f : friendMapper.selectFriends(userId)) {
            Integer fid = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            User u = userMapper.selectById(fid);
            if (u == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("friendshipId", f.getId()); item.put("id", u.getId()); item.put("uid", u.getUid());
            item.put("username", u.getUsername()); item.put("avatar", u.getAvatar());
            item.put("vipLevel", u.getVipLevel()); item.put("addedAt", f.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override
    public List<Map<String, Object>> getPendingRequests(Integer userId) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (Friend f : friendMapper.selectPendingRequests(userId)) {
            User u = userMapper.selectById(f.getUserId());
            if (u == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("friendshipId", f.getId()); item.put("id", u.getId()); item.put("uid", u.getUid());
            item.put("username", u.getUsername()); item.put("avatar", u.getAvatar());
            item.put("vipLevel", u.getVipLevel()); item.put("requestedAt", f.getCreatedAt()); r.add(item);
        }
        return r;
    }

    @Override
    public void sendRequest(Integer userId, Integer targetUserId) {
        if (userId.equals(targetUserId)) throw new BusinessException("不能添加自己为好友", 400);
        if (userMapper.selectById(targetUserId) == null) throw new BusinessException("目标用户不存在", 404);
        if (friendMapper.countExisting(userId, targetUserId) > 0) throw new BusinessException("已存在好友关系或待处理请求", 409);
        Friend f = new Friend(); f.setUserId(userId); f.setFriendId(targetUserId); f.setStatus("pending"); friendMapper.insert(f);
    }

    @Override
    public void acceptRequest(Integer userId, Integer requestId) {
        Friend f = friendMapper.selectById(requestId);
        if (f == null || !f.getFriendId().equals(userId) || !"pending".equals(f.getStatus())) throw new BusinessException("请求不存在", 404);
        friendMapper.updateStatus(requestId, "accepted");
    }

    @Override
    public void rejectRequest(Integer userId, Integer requestId) {
        Friend f = friendMapper.selectById(requestId);
        if (f == null || !f.getFriendId().equals(userId) || !"pending".equals(f.getStatus())) throw new BusinessException("请求不存在", 404);
        friendMapper.updateStatus(requestId, "rejected");
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        Friend f = friendMapper.selectById(friendId);
        if (f == null) throw new BusinessException("好友关系不存在", 404);
        if (!f.getUserId().equals(userId) && !f.getFriendId().equals(userId)) throw new BusinessException("无权操作", 403);
        friendMapper.deleteById(friendId);
    }
}
