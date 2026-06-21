package com.blank.app.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface VipService {
    List<Map<String, Object>> getVipPlans();
    Map<String, Object> createVipPayment(Integer userId, String planKey, String payType, HttpServletRequest request);
    void handlePaymentNotify(HttpServletRequest request, HttpServletResponse response);
    Map<String, Object> getLatestVipPayment(Integer userId);
    Map<String, Object> cancelVipPayment(Integer userId, String orderNo);
    Map<String, Object> getVipPaymentStatus(Integer userId, String orderNo);
    Map<String, Object> redeemActivationCode(Integer userId, String code);
}
