package com.blank.app.service.impl;

import com.blank.app.entity.*;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.*;
import com.blank.app.service.CheckinService;
import com.blank.app.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CheckinServiceImpl implements CheckinService {

    @Autowired private CheckinMapper checkinMapper;
    @Autowired private DailyTaskRewardMapper dtrMapper;
    @Autowired private PostcardMapper postcardMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private RedisKeyBuilder redisKeyBuilder;

    private static final ZoneId SH = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Object> getStatus(Integer userId) {
        LocalDate today = LocalDate.now(SH);
        YearMonth month = YearMonth.from(today);
        List<Checkin> checkins = checkinMapper.selectByUserAndMonth(userId, month.atDay(1), month.atEndOfMonth());

        List<Integer> checkedDays = new ArrayList<>();
        boolean todayChecked = false;
        int consecutive = 0;
        for (Checkin c : checkins) { checkedDays.add(c.getCheckinDate().getDayOfMonth()); if (c.getCheckinDate().equals(today)) todayChecked = true; }
        LocalDate cursor = todayChecked ? today : today.minusDays(1);
        for (Checkin c : checkins) { if (c.getCheckinDate().equals(cursor)) { consecutive++; cursor = cursor.minusDays(1); } }

        LocalDateTime sod = today.atStartOfDay();
        LocalDateTime eod = today.plusDays(1).atStartOfDay();
        boolean hasSent = postcardMapper.countByUserIdAndToday(userId, sod, eod) > 0;
        boolean claimed = dtrMapper.selectByUserTaskDate(userId, "send_postcard", today) != null;
        User user = userMapper.selectById(userId);
        boolean isVip = user.getVipLevel() != null && !"VIP 0".equals(user.getVipLevel());

        Map<String, Object> task = new LinkedHashMap<>();
        task.put("hasSentPostcard", hasSent); task.put("claimedReward", claimed);
        task.put("reward", 20); task.put("vipBonus", isVip ? 10 : 0); task.put("totalReward", isVip ? 30 : 20);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("checkedDays", checkedDays); r.put("isCheckedInToday", todayChecked);
        r.put("consecutiveDays", consecutive); r.put("month", today.getMonthValue());
        r.put("year", today.getYear()); r.put("postcardTask", task); return r;
    }

    @Override
    public Map<String, Object> doCheckin(Integer userId) {
        LocalDate today = LocalDate.now(SH);
        String rk = redisKeyBuilder.checkin(userId, today.format(DF));
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rk))) throw new BusinessException("今日已签到", 400);
        if (checkinMapper.selectByUserAndDate(userId, today) != null) throw new BusinessException("今日已签到", 400);
        Checkin c = new Checkin(); c.setUserId(userId); c.setCheckinDate(today); c.setCoinsEarned(10); checkinMapper.insert(c);
        User user = userMapper.selectById(userId); user.setCoins(user.getCoins() + 10); userMapper.updateById(user);
        long sec = Duration.between(LocalDateTime.now(SH), today.plusDays(1).atStartOfDay()).getSeconds();
        redisTemplate.opsForValue().set(rk, "1", sec, TimeUnit.SECONDS);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("coinsEarned", 10); r.put("newBalance", user.getCoins()); return r;
    }

    @Override @Transactional
    public Map<String, Object> claimPostcardTaskReward(Integer userId) {
        LocalDate today = LocalDate.now(SH);
        LocalDateTime sod = today.atStartOfDay(), eod = today.plusDays(1).atStartOfDay();
        if (postcardMapper.countByUserIdAndToday(userId, sod, eod) == 0) throw new BusinessException("今天还没有发送明信片", 400);
        if (dtrMapper.selectByUserTaskDate(userId, "send_postcard", today) != null) throw new BusinessException("今日已领取过奖励", 400);
        User user = userMapper.selectById(userId);
        boolean isVip = user.getVipLevel() != null && !"VIP 0".equals(user.getVipLevel());
        int reward = isVip ? 30 : 20;
        DailyTaskReward dtr = new DailyTaskReward();
        dtr.setUserId(userId); dtr.setTaskType("send_postcard"); dtr.setRewardDate(today); dtr.setCoinsEarned(reward); dtrMapper.insert(dtr);
        user.setCoins(user.getCoins() + reward); userMapper.updateById(user);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("coinsEarned", reward); r.put("newBalance", user.getCoins()); return r;
    }
}
