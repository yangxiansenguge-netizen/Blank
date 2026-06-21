package com.blank.app.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blank.app.entity.Notification;
import com.blank.app.entity.Postcard;
import com.blank.app.mapper.NotificationMapper;
import com.blank.app.mapper.PostcardMapper;
import com.blank.app.service.ModerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModerationServiceImpl implements ModerationService {
    private static final Logger log = LoggerFactory.getLogger(ModerationServiceImpl.class);

    private static final String DASHSCOPE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private static final String PROMPT_IMAGE = "你是一个内容安全审核员。请审核这张图片是否包含以下违规内容：\n1. 色情、裸露或性暗示内容\n2. 血腥暴力或恐怖内容\n3. 毒品或违禁药物相关内容\n4. 政治敏感内容(国旗、敏感国家或地区、政治敏感内容、特别是台湾问题)\n5. 其他违法或不良信息\n6. 图片里的文字包含上面敏感内容\n\n请严格按照以下JSON格式回复，不要添加任何其他文字：\n{\"pass\": true} 或 {\"pass\": false, \"reason\": \"具体违规原因\"}";

    private static final String PROMPT_TEXT = "你是一个内容安全审核员。请审核以下明信片文字内容是否包含违规信息：\n1. 色情、低俗或性暗示内容\n2. 血腥暴力或恐怖内容\n3. 毒品或违禁药物相关内容\n4. 政治敏感内容(特别是台湾问题)\n5. 辱骂、歧视或仇恨言论\n6. 其他违法或不良信息\n\n请严格按照以下JSON格式回复，不要添加任何其他文字：\n{\"pass\": true} 或 {\"pass\": false, \"reason\": \"具体违规原因\"}\n\n待审核文字内容：\n";

    @Value("${app.ai.dashscope-api-key:}")
    private String apiKey;

    @Autowired private PostcardMapper postcardMapper;
    @Autowired private NotificationMapper notificationMapper;

    @Override
    public void moderatePostcard(Integer postcardId) {
        try {
            Postcard p = postcardMapper.selectById(postcardId);
            if (p == null) return;

            // Collect text content
            StringBuilder allText = new StringBuilder(p.getTitle() != null ? p.getTitle() : "");
            try {
                List<Map<String, Object>> elements = JSON.parseObject(
                        p.getElements() != null ? p.getElements() : "[]",
                        new TypeReference<List<Map<String, Object>>>(){});
                for (Map<String, Object> el : elements) {
                    if ("text".equals(el.get("type")) && el.get("content") != null) {
                        allText.append("\n").append(el.get("content"));
                    }
                }
            } catch (Exception ignored) {}

            // Run image and text moderation in parallel
            boolean imagePass = moderateImage(p.getImageUrl());
            String textContent = allText.toString().trim();
            boolean textPass = textContent.isEmpty() || moderateText(textContent);

            if (imagePass && textPass) {
                p.setStatus("sent");
                postcardMapper.updateById(p);
            } else {
                List<String> reasons = new ArrayList<>();
                if (!imagePass) reasons.add("图片审核未通过");
                if (!textPass) reasons.add("文字审核未通过");
                String reason = String.join("; ", reasons);

                p.setStatus("pending");
                p.setReviewReason(reason);
                postcardMapper.updateById(p);

                // Notify user
                Notification notif = new Notification();
                notif.setUserId(p.getUserId());
                notif.setType("review");
                notif.setTitle("明信片审核中");
                notif.setContent("您的明信片\"" + (p.getTitle() != null ? p.getTitle() : "未命名") + "\"未通过自动审核（" + reason + "），已提交人工审核，请耐心等待。");
                notif.setPostcardId(postcardId);
                notif.setIsRead(0);
                notificationMapper.insert(notif);
            }
        } catch (Exception e) {
            log.error("Moderation failed for postcard {}", postcardId, e);
            // Fail-open: auto approve
            try {
                Postcard p = postcardMapper.selectById(postcardId);
                if (p != null) {
                    p.setStatus("sent");
                    postcardMapper.updateById(p);
                }
            } catch (Exception ignored) {}
        }
    }

    private boolean moderateImage(String imageUrl) {
        if (apiKey == null || apiKey.isEmpty()) return true;
        try {
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", PROMPT_IMAGE);
            content.add(textPart);
            Map<String, Object> imgPart = new LinkedHashMap<>();
            imgPart.put("type", "image_url");
            Map<String, String> imgUrl = new LinkedHashMap<>();
            imgUrl.put("url", imageUrl);
            imgPart.put("image_url", imgUrl);
            content.add(imgPart);

            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("role", "user");
            msg.put("content", content);

            return callDashScope("qwen3.5-flash", msg);
        } catch (Exception e) {
            log.error("Image moderation error", e);
            return true;
        }
    }

    private boolean moderateText(String text) {
        if (apiKey == null || apiKey.isEmpty()) return true;
        if (text == null || text.isEmpty()) return true;
        try {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("role", "user");
            msg.put("content", PROMPT_TEXT + text);

            return callDashScope("qwen-turbo", msg);
        } catch (Exception e) {
            log.error("Text moderation error", e);
            return true;
        }
    }

    private boolean callDashScope(String model, Map<String, Object> userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sysMsg = new LinkedHashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一个专业的内容安全审核系统。");
        messages.add(sysMsg);
        messages.add(userMessage);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", 200);
        body.put("temperature", 0.1);

        try (HttpResponse resp = HttpRequest.post(DASHSCOPE_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(body))
                .timeout(60000)
                .execute()) {

            String respBody = resp.body();
            if (respBody == null || respBody.isEmpty()) return true;
            JSONObject json = JSON.parseObject(respBody);
            if (json == null || json.getJSONArray("choices") == null || json.getJSONArray("choices").isEmpty())
                return true;
            JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
            if (choice == null || choice.getJSONObject("message") == null) return true;
            String raw = choice.getJSONObject("message").getString("content");
            if (raw == null) return true;

            // Extract JSON
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start >= 0 && end > start) {
                JSONObject parsed = JSON.parseObject(raw.substring(start, end + 1));
                return parsed.getBooleanValue("pass", true);
            }
            return true;
        }
    }
}
