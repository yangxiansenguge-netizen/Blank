package com.blank.app.service;

import java.util.Map;

public interface AiService {
    Map<String, Object> polishText(Integer userId, String text);
    Map<String, Object> generateFromImage(Integer userId, String imageUrl);
    Map<String, Object> customAi(Integer userId, String text, String requirement);
}
