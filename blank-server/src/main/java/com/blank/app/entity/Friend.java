package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friend {
    private Integer id;
    private Integer userId;
    private Integer friendId;
    private String status;
    private LocalDateTime createdAt;
}
