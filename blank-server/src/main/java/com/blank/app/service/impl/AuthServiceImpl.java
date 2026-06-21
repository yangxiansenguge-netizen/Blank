package com.blank.app.service.impl;

import com.blank.app.dto.request.*;
import com.blank.app.dto.response.LoginResponse;
import com.blank.app.entity.User;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.UserMapper;
import com.blank.app.security.JwtTokenProvider;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.AuthService;
import com.blank.app.service.EmailService;
import com.blank.app.util.CodeGenerator;
import com.blank.app.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired private UserMapper userMapper;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private RedisKeyBuilder redisKeyBuilder;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private CodeGenerator codeGenerator;
    @Value("${app.email.verify-expiry:3600}") private int verifyExpiry;

    private static final String[] DEFAULT_AVATARS = {
        "https://blank-img.oss-cn-beijing.aliyuncs.com/stamps/1_1781859010718.png",
        "https://blank-img.oss-cn-beijing.aliyuncs.com/stamps/2_1781858995151.png",
        "https://blank-img.oss-cn-beijing.aliyuncs.com/stamps/3_1781858978590.png",
        "https://blank-img.oss-cn-beijing.aliyuncs.com/stamps/4_1781858964376.png",
        "https://blank-img.oss-cn-beijing.aliyuncs.com/stamps/5_1781858851095.png"
    };
    private static final Random RANDOM = new Random();

    @Override
    public void sendVerifyCode(SendVerifyCodeRequest req) {
        String email = req.getEmail();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKeyBuilder.verifyCooldown(email))))
            throw new BusinessException("验证码发送过于频繁，请60秒后重试", 429);
        String code = codeGenerator.generateVerifyCode();
        redisTemplate.opsForValue().set(redisKeyBuilder.verifyCode(email), code, verifyExpiry, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(redisKeyBuilder.verifyCooldown(email), "1", 60, TimeUnit.SECONDS);
        log.info("验证码: {} -> {}", email, code);
        int mins = verifyExpiry / 60;
        CompletableFuture.runAsync(() -> { try { emailService.sendVerificationEmail(email, code, mins); } catch (Exception e) { log.warn("邮件发送失败 {}: {}", email, e.getMessage()); } });
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        String email = req.getEmail();
        String stored = redisTemplate.opsForValue().get(redisKeyBuilder.verifyCode(email));
        if (stored == null || !stored.equals(req.getVerifyCode())) throw new BusinessException("验证码错误或已过期");
        if (userMapper.countByEmail(email) > 0) throw new BusinessException("该邮箱已被注册", 409);
        if (userMapper.countByUsername(req.getUsername()) > 0) throw new BusinessException("该用户名已被使用", 409);

        String uid;
        do { uid = codeGenerator.generateUid(); } while (userMapper.countByUid(uid) > 0);

        User user = new User();
        user.setUid(uid); user.setUsername(req.getUsername()); user.setEmail(email);
        user.setIdentity("user"); user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setAvatar(DEFAULT_AVATARS[RANDOM.nextInt(DEFAULT_AVATARS.length)]);
        user.setVipLevel("VIP 0"); user.setCoins(100); user.setGender("保密"); user.setProfileVisibility("所有人");
        userMapper.insert(user);
        redisTemplate.delete(redisKeyBuilder.verifyCode(email));
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectByEmail(req.getEmail());
        if (user == null) throw new BusinessException("账号不存在", 404);
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) throw new BusinessException("密码错误");
        syncVipStatus(user);
        return buildLoginResponse(user);
    }

    @Override
    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) jwtTokenProvider.blacklistToken(bearerToken.substring(7));
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail();
        if (userMapper.countByEmail(email) == 0) throw new BusinessException("该邮箱未注册", 404);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKeyBuilder.verifyCooldown(email))))
            throw new BusinessException("验证码发送过于频繁，请60秒后重试", 429);
        String code = codeGenerator.generateVerifyCode();
        redisTemplate.opsForValue().set(redisKeyBuilder.verifyCode(email), code, verifyExpiry, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(redisKeyBuilder.verifyCooldown(email), "1", 60, TimeUnit.SECONDS);
        log.info("找回密码验证码: {} -> {}", email, code);
        int mins = verifyExpiry / 60;
        CompletableFuture.runAsync(() -> { try { emailService.sendVerificationEmail(email, code, mins); } catch (Exception e) { log.warn("邮件发送失败 {}: {}", email, e.getMessage()); } });
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        String email = req.getEmail();
        String stored = redisTemplate.opsForValue().get(redisKeyBuilder.verifyCode(email));
        if (stored == null || !stored.equals(req.getVerifyCode())) throw new BusinessException("验证码错误或已过期");
        User user = userMapper.selectByEmail(email);
        if (user == null) throw new BusinessException("用户不存在", 404);
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
        redisTemplate.delete(redisKeyBuilder.verifyCode(email));
    }

    private LoginResponse buildLoginResponse(User user) {
        JwtUserDetails ud = new JwtUserDetails(user.getId(), user.getUid(), user.getUsername(), user.getEmail(), user.getIdentity());
        String token = jwtTokenProvider.generateToken(ud);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId()); info.put("uid", user.getUid()); info.put("username", user.getUsername());
        info.put("email", user.getEmail()); info.put("identity", user.getIdentity()); info.put("avatar", user.getAvatar());
        info.put("coins", user.getCoins()); info.put("vipLevel", user.getVipLevel());
        info.put("vipPlanKey", user.getVipPlanKey()); info.put("vipExpiresAt", user.getVipExpiresAt());
        info.put("gender", user.getGender()); info.put("birthday", user.getBirthday());
        info.put("location", user.getLocation()); info.put("profileVisibility", user.getProfileVisibility());
        info.put("createdAt", user.getCreatedAt());
        LoginResponse resp = new LoginResponse(); resp.setToken(token); resp.setUserInfo(info); return resp;
    }

    private void syncVipStatus(User user) {
        if (!"VIP 0".equals(user.getVipLevel()) && !"lifetime".equals(user.getVipPlanKey())
                && user.getVipExpiresAt() != null && user.getVipExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            user.setVipLevel("VIP 0"); user.setVipPlanKey(null); user.setVipExpiresAt(null); userMapper.updateById(user);
        }
    }
}
