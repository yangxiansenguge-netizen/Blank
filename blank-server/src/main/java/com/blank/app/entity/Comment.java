package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer postcardId;
    private Integer userId;
    private String content;
    private Integer isPinned;
    private Integer likesCount;
    private LocalDateTime createdAt;
}
