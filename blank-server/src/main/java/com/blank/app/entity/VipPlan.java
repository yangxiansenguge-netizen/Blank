package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VipPlan {
    private Integer id;
    private String planKey;
    private String title;
    private String billingUnit;
    private Integer priceCents;
    private String currency;
    private Integer isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
