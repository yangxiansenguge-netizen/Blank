package com.blank.app.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blank.app.entity.User;
import com.blank.app.exception.BusinessException;
import com.blank.app.mapper.UserMapper;
import com.blank.app.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    private static final String DASHSCOPE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @Value("${app.ai.dashscope-api-key:}")
    private String apiKey;
    @Value("${app.ai.text-cost:5}")
    private int textCost;
    @Value("${app.ai.image-cost:10}")
    private int imageCost;

    @Autowired private UserMapper userMapper;

    @Override
    public Map<String, Object> polishText(Integer userId, String text) {
        if (text == null || text.trim().isEmpty()) throw new BusinessException("请输入文字", 400);
        User user = deductCoins(userId, textCost);

        String prompt = "请润色以下明信片文字，使其更优美、更有诗意，但保持原意。直接返回润色后的文字，不要添加任何解释或前缀：\n\n\"" + text + "\"";
        String result = callDashScope("qwen-turbo", prompt, null);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", result);
        resp.put("newBalance", user.getCoins());
        return resp;
    }

    @Override
    public Map<String, Object> generateFromImage(Integer userId, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) throw new BusinessException("请提供图片", 400);
        User user = deductCoins(userId, imageCost);

        String prompt = "请根据这张明信片图片，生成一段优美、简短、富有诗意的文案，适合写在明信片上。直接返回文案内容，不要添加任何解释或前缀。";

        // Call with image
        String result = callDashScope("qwen3.5-flash", prompt, imageUrl);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", result);
        resp.put("newBalance", user.getCoins());
        return resp;
    }

    @Override
    public Map<String, Object> customAi(Integer userId, String text, String requirement) {
        if (text == null || text.trim().isEmpty()) throw new BusinessException("请输入文字", 400);
        User user = deductCoins(userId, textCost);

        String prompt = "请根据以下要求修改这段明信片文字：\n\n要求：" + requirement + "\n\n原文：\"" + text + "\"\n\n直接返回修改后的文字，不要添加任何解释或前缀。";

        String result = callDashScope("qwen-turbo", prompt, null);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", result);
        resp.put("newBalance", user.getCoins());
        return resp;
    }

    private User deductCoins(Integer userId, int cost) {
        User user = userMapper.selectById(userId);
        boolean isVip = user.getVipLevel() != null && !"VIP 0".equals(user.getVipLevel());
        int actualCost = isVip ? 0 : cost;

        if (user.getCoins() < actualCost) throw new BusinessException("邮分不足", 400);

        if (actualCost > 0) {
            user.setCoins(user.getCoins() - actualCost);
            userMapper.updateById(user);
        }
        return user;
    }

    private String callDashScope(String model, String prompt, String imageUrl) {
        if (apiKey == null || apiKey.isEmpty()) throw new BusinessException("AI功能未配置", 500);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");

        if (imageUrl != null) {
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", prompt);
            content.add(textPart);
            Map<String, Object> imgPart = new LinkedHashMap<>();
            imgPart.put("type", "image_url");
            Map<String, String> img = new LinkedHashMap<>();
            img.put("url", imageUrl);
            imgPart.put("image_url", img);
            content.add(imgPart);
            userMsg.put("content", content);
        } else {
            userMsg.put("content", prompt);
        }
        messages.add(userMsg);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", 500);

        try (HttpResponse resp = HttpRequest.post(DASHSCOPE_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(body))
                .timeout(60000)
                .execute()) {

            JSONObject json = JSON.parseObject(resp.body());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        }
    }
}
