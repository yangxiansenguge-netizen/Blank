package com.blank.app.service;

import java.util.Map;

public interface CheckinService {
    Map<String, Object> getStatus(Integer userId);
    Map<String, Object> doCheckin(Integer userId);
    Map<String, Object> claimPostcardTaskReward(Integer userId);
}
