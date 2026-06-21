package com.blank.app.controller;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.security.CurrentUser;
import com.blank.app.security.JwtUserDetails;
import com.blank.app.service.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vip")
public class VipController {

    @Autowired
    private VipService vipService;

    @GetMapping("/plans")
    public ApiResponse<List<Map<String, Object>>> getVipPlans() {
        return ApiResponse.success(vipService.getVipPlans());
    }

    @PostMapping("/payments/create")
    public ApiResponse<Map<String, Object>> createVipPayment(
            @CurrentUser JwtUserDetails user,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        return ApiResponse.success(vipService.createVipPayment(user.getId(),
                body.get("planKey"), body.get("payType"), request));
    }

    @RequestMapping(value = "/payments/notify", method = {RequestMethod.GET, RequestMethod.POST})
    public void handlePaymentNotify(HttpServletRequest request, HttpServletResponse response) {
        vipService.handlePaymentNotify(request, response);
    }

    @GetMapping("/payments/latest")
    public ApiResponse<Map<String, Object>> getLatestVipPayment(@CurrentUser JwtUserDetails user) {
        return ApiResponse.success(vipService.getLatestVipPayment(user.getId()));
    }

    @PostMapping("/payments/{orderNo}/cancel")
    public ApiResponse<Map<String, Object>> cancelVipPayment(
            @CurrentUser JwtUserDetails user,
            @PathVariable String orderNo) {
        return ApiResponse.success(vipService.cancelVipPayment(user.getId(), orderNo), "订单已取消");
    }

    @GetMapping("/payments/{orderNo}")
    public ApiResponse<Map<String, Object>> getVipPaymentStatus(
            @CurrentUser JwtUserDetails user,
            @PathVariable String orderNo) {
        return ApiResponse.success(vipService.getVipPaymentStatus(user.getId(), orderNo));
    }

    @PostMapping("/activation/redeem")
    public ApiResponse<Map<String, Object>> redeemActivationCode(
            @CurrentUser JwtUserDetails user,
            @RequestBody Map<String, String> body) {
        return ApiResponse.success(vipService.redeemActivationCode(user.getId(), body.get("code")), "激活成功");
    }
}
