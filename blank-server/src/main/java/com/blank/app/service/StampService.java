package com.blank.app.service;

import java.util.List;
import java.util.Map;

public interface StampService {
    List<Map<String, Object>> getStampSeries();
    List<Map<String, Object>> getStamps(String category, Integer userId);
    List<Map<String, Object>> getMyStamps(Integer userId);
    Map<String, Object> purchaseStamp(Integer userId, Integer stampId);
}
