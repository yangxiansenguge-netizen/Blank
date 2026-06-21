package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentLike {
    private Integer id;
    private Integer userId;
    private Integer commentId;
    private LocalDateTime createdAt;
}
