package com.blank.app.service.impl;

import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.StampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StampServiceImpl implements StampService {

    @Autowired private StampMapper stampMapper;
    @Autowired private StampSeriesMapper stampSeriesMapper;
    @Autowired private UserStampMapper userStampMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private StringRedisTemplate redisTemplate;

    @Override
    public List<Map<String, Object>> getStampSeries() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (StampSeries s : stampSeriesMapper.selectAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId()); item.put("name", s.getName());
            item.put("description", s.getDescription()); item.put("sortOrder", s.getSortOrder());
            r.add(item);
        }
        return r;
    }

    @Override
    public List<Map<String, Object>> getStamps(String category, Integer userId) {
        List<Stamp> stamps = (category != null && !category.isEmpty() && !"\u5168\u90E8".equals(category))
                ? stampMapper.selectBySeriesId(category) : stampMapper.selectAll();
        Map<Integer, UserStamp> ownedMap = new HashMap<>();
        if (userId != null) for (UserStamp us : userStampMapper.selectByUserId(userId)) ownedMap.put(us.getStampId(), us);
        List<Map<String, Object>> r = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Stamp s : stamps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId()); item.put("seriesId", s.getSeriesId()); item.put("title", s.getTitle());
            item.put("description", s.getDescription()); item.put("price", s.getPrice()); item.put("image", s.getImagePath());
            UserStamp owned = ownedMap.get(s.getId());
            item.put("ownedQuantity", owned != null ? owned.getQuantity() : 0);
            boolean pt = owned != null && owned.getPurchasedAt() != null && owned.getPurchasedAt().toLocalDate().equals(today);
            item.put("purchasedToday", pt); item.put("canPurchaseToday", !pt); r.add(item);
        }
        return r;
    }

    @Override
    public List<Map<String, Object>> getMyStamps(Integer userId) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (UserStamp us : userStampMapper.selectByUserId(userId)) {
            Stamp s = stampMapper.selectById(us.getStampId());
            if (s == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId()); item.put("stampId", s.getId()); item.put("title", s.getTitle());
            item.put("description", s.getDescription()); item.put("price", s.getPrice());
            item.put("image", s.getImagePath()); item.put("seriesId", s.getSeriesId());
            item.put("quantity", us.getQuantity()); item.put("purchasedAt", us.getPurchasedAt()); r.add(item);
        }
        return r;
    }

    @Override @Transactional
    public Map<String, Object> purchaseStamp(Integer userId, Integer stampId) {
        Stamp stamp = stampMapper.selectByIdForUpdate(stampId);
        if (stamp == null) throw new BusinessException("邮票不存在", 404);
        User user = userMapper.selectById(userId);
        syncVip(user);
        if (user.getCoins() < stamp.getPrice()) throw new BusinessException("邮分不足", 400);

        // Daily limit via Redis (prevents duplicate within same day)
        String redisKey = "blank:stamp_purchase:" + userId + ":" + stampId + ":" + LocalDate.now();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)))
            throw new BusinessException("今日已购买过此邮票", 409);

        user.setCoins(user.getCoins() - stamp.getPrice()); userMapper.updateById(user);

        List<UserStamp> owned = userStampMapper.selectByUserAndStamp(userId, stampId);
        if (!owned.isEmpty()) {
            UserStamp us = owned.get(0); us.setQuantity(us.getQuantity() + 1);
            us.setPurchasedAt(LocalDateTime.now()); userStampMapper.updateById(us);
        } else {
            UserStamp us = new UserStamp(); us.setUserId(userId); us.setStampId(stampId);
            us.setQuantity(1); us.setPurchasedAt(LocalDateTime.now()); userStampMapper.insert(us);
        }

        // Cache daily limit in Redis until end of day
        redisTemplate.opsForValue().set(redisKey, "1",
                java.time.Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).getSeconds(),
                java.util.concurrent.TimeUnit.SECONDS);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("coins", user.getCoins()); r.put("newBalance", user.getCoins()); r.put("stampId", stampId); r.put("stampTitle", stamp.getTitle()); return r;
    }

    private void syncVip(User user) {
        if (!"VIP 0".equals(user.getVipLevel()) && !"lifetime".equals(user.getVipPlanKey())
                && user.getVipExpiresAt() != null && user.getVipExpiresAt().isBefore(LocalDateTime.now())) {
            user.setVipLevel("VIP 0"); user.setVipPlanKey(null); user.setVipExpiresAt(null); userMapper.updateById(user);
        }
    }
}
