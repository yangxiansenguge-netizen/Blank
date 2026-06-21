package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserStamp {
    private Integer id;
    private Integer userId;
    private Integer stampId;
    private Integer quantity;
    private LocalDateTime purchasedAt;
}
