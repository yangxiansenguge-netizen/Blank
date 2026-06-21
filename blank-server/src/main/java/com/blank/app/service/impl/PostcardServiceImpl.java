package com.blank.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.blank.app.dto.request.BatchDeleteRequest;
import com.blank.app.dto.request.CreatePostcardRequest;
import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.FileStorageService;
import com.blank.app.service.ModerationService;
import com.blank.app.service.PostcardService;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PostcardServiceImpl implements PostcardService {
    private static final Logger log = LoggerFactory.getLogger(PostcardServiceImpl.class);

    @Autowired private PostcardMapper postcardMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private UserStampMapper userStampMapper;
    @Autowired private StampMapper stampMapper;
    @Autowired private LikeMapper likeMapper;
    @Autowired private FriendMapper friendMapper;
    @Autowired private CommentMapper commentMapper;
    @Value("${app.ai.dashscope-api-key:}") private String dashscopeApiKey;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private ModerationService moderationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> getDiscover(Integer page, Integer pageSize, Integer userId) {
        PageHelper.startPage(page, pageSize);
        List<Postcard> records = postcardMapper.selectDiscoverPage();
        long total = new com.github.pagehelper.PageInfo<>(records).getTotal();
        return buildPageResult(buildPostcardList(records, userId), total, page, pageSize);
    }

    @Override
    public Map<String, Object> getDrifting(Integer page, Integer pageSize, Integer userId) {
        PageHelper.startPage(page, pageSize);
        List<Postcard> records = postcardMapper.selectDriftingPage();
        long total = new com.github.pagehelper.PageInfo<>(records).getTotal();
        return buildPageResult(buildPostcardList(records, userId), total, page, pageSize);
    }

    @Override
    public Map<String, Object> getDetail(Integer id, Integer userId) {
        Postcard p = postcardMapper.selectById(id);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        User author = userMapper.selectById(p.getUserId());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", p.getId()); detail.put("title", p.getTitle());
        detail.put("imageUrl", p.getImageUrl());
        Map<String, Object> off = new LinkedHashMap<>();
        off.put("x", p.getImageOffsetX() != null ? p.getImageOffsetX() : 0f);
        off.put("y", p.getImageOffsetY() != null ? p.getImageOffsetY() : 0f);
        detail.put("imageOffset", off);
        detail.put("imageScale", p.getImageScale()); detail.put("imageRotation", p.getImageRotation());
        detail.put("aspectRatio", p.getAspectRatio()); detail.put("canvasWidth", p.getCanvasWidth());
        detail.put("canvasHeight", p.getCanvasHeight()); detail.put("postcardType", p.getPostcardType());
        detail.put("status", p.getStatus()); detail.put("isPublic", p.getIsPublic() == 1);
        detail.put("recipientInput", p.getRecipientInput());
        detail.put("authorName", author != null ? author.getUsername() : "");
        detail.put("authorUid", author != null ? author.getUid() : "");
        detail.put("authorAvatar", author != null ? author.getAvatar() : "");
        detail.put("isOwner", userId != null && p.getUserId().equals(userId));
        detail.put("createdAt", p.getCreatedAt()); detail.put("reviewReason", p.getReviewReason());
        detail.put("scheduledAt", p.getScheduledAt());
        try { detail.put("elements", p.getElements() != null ? JSON.parseArray(p.getElements()) : new ArrayList<>()); }
        catch (Exception e) { detail.put("elements", new ArrayList<>()); }
        if (p.getStampId() != null) {
            Stamp s = stampMapper.selectById(p.getStampId());
            if (s != null) {
                Map<String, Object> si = new LinkedHashMap<>();
                si.put("id", s.getId()); si.put("title", s.getTitle()); si.put("image", s.getImagePath());
                detail.put("stamp", si);
            }
        }
        if (userId != null) { detail.put("isLiked", likeMapper.selectByUserAndPostcard(userId, id) != null); }
        else { detail.put("isLiked", false); }
        detail.put("likeCount", (int) likeMapper.countByPostcardId(id));
        return detail;
    }

    @Override @Transactional
    public Map<String, Object> create(Integer userId, CreatePostcardRequest req) {
        Stamp stamp = stampMapper.selectById(req.getStampId());
        if (stamp == null) throw new BusinessException("邮票不存在", 404);

        List<UserStamp> ownedStamps = userStampMapper.selectByUserAndStamp(userId, req.getStampId());
        if (ownedStamps.isEmpty()) throw new BusinessException("你还没有这张邮票", 400);
        UserStamp ownedStamp = ownedStamps.get(0);
        if (ownedStamp.getQuantity() <= 0) throw new BusinessException("这张邮票已经没有剩余了", 400);

        User user = userMapper.selectById(userId);
        syncVipStatus(user);
        boolean isVip = user.getVipLevel() != null && !"VIP 0".equals(user.getVipLevel());
        String postcardType = req.getPostcardType() != null ? req.getPostcardType() : "normal";
        String recipientInput = "";
        boolean isScheduled = false;
        LocalDateTime scheduledAt = null;

        if ("normal".equals(postcardType)) {
            String rawRecipient = req.getRecipientInput() != null ? req.getRecipientInput().trim() : "";
            if (rawRecipient.isEmpty()) throw new BusinessException("请填写收件人UID或邮箱", 400);
            User recipient = userMapper.selectByUid(rawRecipient);
            if (recipient == null) recipient = userMapper.selectByEmail(rawRecipient);
            if (recipient == null) throw new BusinessException("收件人不存在", 404);
            recipientInput = recipient.getUid();
            if (req.getScheduledAt() != null && !req.getScheduledAt().isEmpty()) {
                scheduledAt = LocalDateTime.parse(req.getScheduledAt(), DATE_FMT);
                if (scheduledAt.isBefore(LocalDateTime.now()))
                    throw new BusinessException("定时发送时间必须晚于当前时间", 400);
                if (!recipient.getId().equals(userId) && friendMapper.checkIsFriend(userId, recipient.getId()) == 0)
                    throw new BusinessException("定时发送仅支持发送给好友或自己", 400);
                isScheduled = true;
            }
        }

        if (!isVip) {
            if (ownedStamp.getQuantity() == 1) userStampMapper.deleteById(ownedStamp.getId());
            else { ownedStamp.setQuantity(ownedStamp.getQuantity() - 1); userStampMapper.updateById(ownedStamp); }
        }

        Postcard p = new Postcard();
        p.setUserId(userId); p.setTitle(req.getTitle() != null ? req.getTitle() : "");
        p.setImageUrl(req.getImageUrl()); p.setAspectRatio(req.getAspectRatio() != null ? req.getAspectRatio() : "3/2");
        p.setCanvasWidth(req.getCanvasWidth() != null ? req.getCanvasWidth() : 600);
        p.setCanvasHeight(req.getCanvasHeight() != null ? req.getCanvasHeight() : 400);
        p.setImageOffsetX(req.getImageOffsetX() != null ? req.getImageOffsetX() : 0f);
        p.setImageOffsetY(req.getImageOffsetY() != null ? req.getImageOffsetY() : 0f);
        p.setImageScale(req.getImageScale() != null ? req.getImageScale() : 1f);
        p.setImageRotation(req.getImageRotation() != null ? req.getImageRotation() : 0f);
        p.setStampId(req.getStampId()); p.setRecipientInput(recipientInput);
        p.setIsPublic(req.getIsPublic() != null && req.getIsPublic() ? 1 : 0);
        p.setPostcardType(postcardType);
        p.setElements(req.getElements() != null ? JSON.toJSONString(req.getElements()) : "[]");
        p.setScheduledAt(scheduledAt);
        p.setStatus(isScheduled ? "scheduled" : (!dashscopeApiKey.isEmpty() ? "reviewing" : "sent"));
        p.setSenderDeleted(0); p.setRecipientDeleted(0);
        postcardMapper.insert(p);

        user.setCoins(user.getCoins() + 20); userMapper.updateById(user);

        if (!isScheduled && !dashscopeApiKey.isEmpty()) {
            final Integer pid = p.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    new Thread(() -> { try { moderationService.moderatePostcard(pid); } catch (Exception e) { log.error("Moderation failed", e); } }).start();
                }
            });
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", p.getId()); result.put("status", p.getStatus());
        result.put("scheduledAt", p.getScheduledAt()); result.put("stampConsumed", !isVip);
        result.put("vipFreePostage", isVip); return result;
    }

    @Override @Transactional
    public void deletePostcard(Integer userId, Integer postcardId) {
        Postcard p = postcardMapper.selectById(postcardId);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        User user = userMapper.selectById(userId);
        boolean isSender = p.getUserId().equals(userId);
        boolean isRecipient = user.getUid().equals(p.getRecipientInput());
        if (!isSender && !isRecipient) throw new BusinessException("无权操作", 403);
        if (isSender) p.setSenderDeleted(1);
        if (isRecipient) p.setRecipientDeleted(1);
        postcardMapper.updateById(p);
        boolean noRecipient = p.getRecipientInput() == null || p.getRecipientInput().isEmpty();
        if (p.getSenderDeleted() == 1 && (p.getRecipientDeleted() == 1 || noRecipient)) {
            commentMapper.deleteByPostcardId(postcardId);
            likeMapper.deleteByPostcardId(postcardId);
            postcardMapper.deleteById(postcardId);
        }
    }

    @Override @Transactional
    public void batchDelete(Integer userId, BatchDeleteRequest req) {
        for (Integer id : req.getIds()) deletePostcard(userId, id);
    }

    @Override
    public Map<String, Object> getInbox(Integer userId, Integer page, Integer pageSize) {
        User user = userMapper.selectById(userId);
        PageHelper.startPage(page, pageSize);
        List<Postcard> records = postcardMapper.selectInboxPage(user.getUid());
        long total = new com.github.pagehelper.PageInfo<>(records).getTotal();
        return buildPageResult(buildPostcardList(records, userId), total, page, pageSize);
    }

    @Override
    public Map<String, Object> getOutbox(Integer userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Postcard> records = postcardMapper.selectOutboxPage(userId);
        long total = new com.github.pagehelper.PageInfo<>(records).getTotal();
        return buildPageResult(buildPostcardList(records, userId), total, page, pageSize);
    }

    @Override
    public Map<String, Object> getFavorites(Integer userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Integer> ids = likeMapper.selectFavoritesPage(userId);
        long total = new com.github.pagehelper.PageInfo<>(ids).getTotal();
        if (ids.isEmpty()) return buildPageResult(new ArrayList<>(), 0, page, pageSize);
        List<Postcard> postcards = new ArrayList<>();
        for (Integer pid : ids) { Postcard p = postcardMapper.selectById(pid); if (p != null) postcards.add(p); }
        return buildPageResult(buildPostcardList(postcards, userId), total, page, pageSize);
    }

    @Override
    public Map<String, Object> toggleLike(Integer userId, Integer postcardId) {
        Like existing = likeMapper.selectByUserAndPostcard(userId, postcardId);
        boolean isLiked;
        if (existing != null) { likeMapper.deleteById(existing.getId()); isLiked = false; }
        else { Like l = new Like(); l.setUserId(userId); l.setPostcardId(postcardId); likeMapper.insert(l); isLiked = true; }
        long count = likeMapper.countByPostcardId(postcardId);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("isLiked", isLiked); r.put("likeCount", (int) count); return r;
    }

    @Override
    public Map<String, String> uploadImage(MultipartFile file) {
        Map<String, String> r = new LinkedHashMap<>(); r.put("imageUrl", fileStorageService.storePostcard(file)); return r;
    }

    @Override
    public Map<String, Object> addDriftElement(Integer pid, Integer uid, String uname, Map<String, Object> el) {
        Postcard p = postcardMapper.selectById(pid);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (!"drifting".equals(p.getPostcardType())) throw new BusinessException("不支持漂流操作", 400);
        List<Map<String, Object>> els = parseElements(p.getElements());
        int tc = 0, sc = 0;
        for (Map<String, Object> e : els) {
            if (uid.equals(e.get("creatorId"))) {
                if ("text".equals(e.get("type"))) tc++; else if ("sticker".equals(e.get("type"))) sc++;
            }
        }
        String t = (String) el.get("type");
        if ("text".equals(t) && tc >= 1) throw new BusinessException("每人最多添加1个文字", 400);
        if ("sticker".equals(t) && sc >= 1) throw new BusinessException("每人最多添加1个贴纸", 400);
        el.put("creatorId", uid); el.put("creatorName", uname); els.add(el);
        p.setElements(JSON.toJSONString(els)); postcardMapper.updateById(p);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("elements", els); return r;
    }

    @Override
    public Map<String, Object> deleteDriftElement(Integer pid, Integer idx, Integer uid) {
        Postcard p = postcardMapper.selectById(pid);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (!"drifting".equals(p.getPostcardType())) throw new BusinessException("不支持漂流操作", 400);
        List<Map<String, Object>> els = parseElements(p.getElements());
        if (idx < 0 || idx >= els.size()) throw new BusinessException("无效索引", 400);
        if (!uid.equals(p.getUserId()) && !uid.equals(els.get(idx).get("creatorId"))) throw new BusinessException("无权删除", 403);
        els.remove(idx.intValue()); p.setElements(JSON.toJSONString(els)); postcardMapper.updateById(p);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("elements", els); return r;
    }

    @Override
    public Map<String, Object> updateDriftElements(Integer pid, Integer uid, String uname, List<Map<String, Object>> nel) {
        Postcard p = postcardMapper.selectById(pid);
        if (p == null) throw new BusinessException("明信片不存在", 404);
        if (!"drifting".equals(p.getPostcardType())) throw new BusinessException("不支持漂流操作", 400);
        List<Map<String, Object>> oel = parseElements(p.getElements());
        if (!uid.equals(p.getUserId())) {
            if (nel.size() != oel.size()) throw new BusinessException("无权添加或删除元素", 403);
            for (int i = 0; i < oel.size(); i++) {
                if (!uid.equals(oel.get(i).get("creatorId"))) {
                    if (!JSON.toJSONString(oel.get(i)).equals(JSON.toJSONString(nel.get(i))))
                        throw new BusinessException("无权修改他人元素", 403);
                } else { nel.get(i).put("creatorId", uid); nel.get(i).put("creatorName", uname); }
            }
        }
        p.setElements(JSON.toJSONString(nel)); postcardMapper.updateById(p);
        Map<String, Object> r = new LinkedHashMap<>(); r.put("elements", nel); return r;
    }

    private List<Map<String, Object>> parseElements(String e) {
        try { return e != null ? JSON.parseObject(e, new TypeReference<List<Map<String, Object>>>(){}) : new ArrayList<>(); }
        catch (Exception ex) { return new ArrayList<>(); }
    }

    private List<Map<String, Object>> buildPostcardList(List<Postcard> records, Integer userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Postcard p : records) {
            User author = userMapper.selectById(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId()); item.put("title", p.getTitle()); item.put("image", p.getImageUrl());
            item.put("postcardType", p.getPostcardType()); item.put("aspectRatio", p.getAspectRatio());
            item.put("canvasWidth", p.getCanvasWidth()); item.put("canvasHeight", p.getCanvasHeight());
            item.put("recipientInput", p.getRecipientInput()); item.put("isPublic", p.getIsPublic());
            item.put("senderDeleted", p.getSenderDeleted()); item.put("recipientDeleted", p.getRecipientDeleted());
            Map<String, Object> off = new LinkedHashMap<>();
            off.put("x", p.getImageOffsetX() != null ? p.getImageOffsetX() : 0f);
            off.put("y", p.getImageOffsetY() != null ? p.getImageOffsetY() : 0f);
            item.put("imageOffset", off);
            item.put("imageScale", p.getImageScale() != null ? p.getImageScale() : 1f);
            item.put("imageRotation", p.getImageRotation() != null ? p.getImageRotation() : 0f);
            item.put("authorName", author != null ? author.getUsername() : "");
            item.put("authorUid", author != null ? author.getUid() : "");
            item.put("authorAvatar", author != null ? author.getAvatar() : "");
            item.put("createdAt", p.getCreatedAt());
            item.put("likeCount", (int) likeMapper.countByPostcardId(p.getId()));
            item.put("isLiked", userId != null && likeMapper.selectByUserAndPostcard(userId, p.getId()) != null);
            if (p.getStampId() != null) {
                Stamp s = stampMapper.selectById(p.getStampId());
                if (s != null) {
                    Map<String, Object> so = new LinkedHashMap<>();
                    so.put("id", s.getId()); so.put("title", s.getTitle());
                    so.put("image", s.getImagePath()); so.put("seriesId", s.getSeriesId());
                    item.put("stamp", so);
                }
            }
            item.put("elements", parseElements(p.getElements()));
            list.add(item);
        }
        return list;
    }

    private Map<String, Object> buildPageResult(List<Map<String, Object>> list, long total, int page, int pageSize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("total", total); p.put("page", page); p.put("pageSize", pageSize);
        p.put("totalPages", (int) Math.ceil((double) total / pageSize));
        result.put("pagination", p); return result;
    }

    private void syncVipStatus(User user) {
        if (!"VIP 0".equals(user.getVipLevel()) && !"lifetime".equals(user.getVipPlanKey())
                && user.getVipExpiresAt() != null && user.getVipExpiresAt().isBefore(LocalDateTime.now())) {
            user.setVipLevel("VIP 0"); user.setVipPlanKey(null); user.setVipExpiresAt(null);
            userMapper.updateById(user);
        }
    }
}
