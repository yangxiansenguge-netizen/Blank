package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Like {
    private Integer id;
    private Integer userId;
    private Integer postcardId;
    private LocalDateTime createdAt;
}
