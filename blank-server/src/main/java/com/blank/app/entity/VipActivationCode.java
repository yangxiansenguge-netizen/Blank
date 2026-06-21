package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VipActivationCode {
    private Integer id;
    private String code;
    private String vipLevel;
    private Integer validDays;
    private String status;
    private String note;
    private Integer usedBy;
    private LocalDateTime usedAt;
    private Integer createdBy;
    private LocalDateTime createdAt;
}
