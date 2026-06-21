package com.blank.app.service.impl;

import com.blank.app.dto.request.DeleteAccountRequest;
import com.blank.app.dto.request.UpdatePasswordRequest;
import com.blank.app.dto.request.UpdateProfileRequest;
import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.FileStorageService;
import com.blank.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserMapper userMapper;
    @Autowired private PostcardMapper postcardMapper;
    @Autowired private LikeMapper likeMapper;
    @Autowired private CommentMapper commentMapper;
    @Autowired private CommentLikeMapper commentLikeMapper;
    @Autowired private FriendMapper friendMapper;
    @Autowired private UserStampMapper userStampMapper;
    @Autowired private CheckinMapper checkinMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FileStorageService fileStorageService;

    private static final Set<String> VALID_VIS = new HashSet<>(Arrays.asList("所有人", "仅好友", "仅自己"));

    @Override
    public Map<String, Object> getProfile(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在", 404);
        syncVip(user);
        return buildProfile(userMapper.selectById(userId));
    }

    @Override
    public Map<String, Object> updateProfile(Integer userId, UpdateProfileRequest req) {
        User user = userMapper.selectById(userId);
        if (req.getUsername() != null && !req.getUsername().isEmpty()) {
            if (userMapper.countByUsernameExcludeSelf(req.getUsername(), userId) > 0)
                throw new BusinessException("该用户名已被使用", 409);
            user.setUsername(req.getUsername());
        }
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getBirthday() != null && !req.getBirthday().isEmpty()) user.setBirthday(LocalDate.parse(req.getBirthday()));
        if (req.getLocation() != null) user.setLocation(req.getLocation());
        if (req.getProfileVisibility() != null) {
            if (!VALID_VIS.contains(req.getProfileVisibility())) throw new BusinessException("无效的可见性设置");
            user.setProfileVisibility(req.getProfileVisibility());
        }
        userMapper.updateById(user);
        return buildProfile(userMapper.selectById(userId));
    }

    @Override
    public Map<String, String> updateAvatar(Integer userId, MultipartFile file) {
        String u = fileStorageService.storeAvatar(file);
        User user = userMapper.selectById(userId);
        user.setAvatar(u); userMapper.updateById(user);
        Map<String, String> r = new HashMap<>(); r.put("avatar", u); return r;
    }

    @Override
    public void updatePassword(Integer userId, UpdatePasswordRequest req) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) throw new BusinessException("旧密码错误");
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword())); userMapper.updateById(user);
    }

    @Override @Transactional
    public void deleteAccount(Integer userId, DeleteAccountRequest req) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) throw new BusinessException("密码错误");
        commentLikeMapper.deleteByUserId(userId);
        commentMapper.deleteByUserId(userId);
        likeMapper.deleteByUserId(userId);
        friendMapper.deleteByUserId(userId);
        userStampMapper.deleteByUserId(userId);
        checkinMapper.deleteByUserId(userId);
        postcardMapper.deleteByUserId(userId);
        userMapper.deleteById(userId);
    }

    @Override
    public Map<String, Object> getStats(Integer userId) {
        User user = userMapper.selectById(userId);
        long sent = postcardMapper.selectByUserId(userId).size();
        long received = user != null ? postcardMapper.selectInboxPage(user.getUid()).size() : 0;
        long likes = likeMapper.countByUserId(userId);
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("sentPostcards", sent); s.put("receivedPostcards", received); s.put("likesReceived", likes); return s;
    }

    @Override
    public List<Map<String, Object>> searchUser(Integer userId, String keyword) {
        List<User> users = userMapper.searchByKeyword(keyword, userId);
        List<Map<String, Object>> r = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId()); item.put("uid", u.getUid()); item.put("username", u.getUsername());
            item.put("avatar", u.getAvatar()); item.put("vipLevel", u.getVipLevel()); r.add(item);
        }
        return r;
    }

    private void syncVip(User user) {
        if (!"VIP 0".equals(user.getVipLevel()) && !"lifetime".equals(user.getVipPlanKey())
                && user.getVipExpiresAt() != null && user.getVipExpiresAt().isBefore(LocalDateTime.now())) {
            user.setVipLevel("VIP 0"); user.setVipPlanKey(null); user.setVipExpiresAt(null); userMapper.updateById(user);
        }
    }

    private Map<String, Object> buildProfile(User user) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", user.getId()); p.put("uid", user.getUid()); p.put("username", user.getUsername());
        p.put("email", user.getEmail()); p.put("identity", user.getIdentity()); p.put("avatar", user.getAvatar());
        p.put("coins", user.getCoins()); p.put("vipLevel", user.getVipLevel());
        p.put("vipPlanKey", user.getVipPlanKey()); p.put("vipExpiresAt", user.getVipExpiresAt());
        p.put("gender", user.getGender()); p.put("birthday", user.getBirthday());
        p.put("location", user.getLocation()); p.put("profileVisibility", user.getProfileVisibility());
        p.put("createdAt", user.getCreatedAt()); return p;
    }
}
