package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VipOrder {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private String planKey;
    private String planTitle;
    private Integer amountCents;
    private String paymentType;
    private String status;
    private String zpayTradeNo;
    private String payUrl;
    private String requestIp;
    private String device;
    private String notifyRaw;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
