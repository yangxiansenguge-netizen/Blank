package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Postcard {
    private Integer id;
    private Integer userId;
    private String title;
    private String imageUrl;
    private Float imageOffsetX;
    private Float imageOffsetY;
    private Float imageScale;
    private Float imageRotation;
    private String aspectRatio;
    private Integer canvasWidth;
    private Integer canvasHeight;
    private Integer stampId;
    private String recipientInput;
    private Integer isPublic;
    private String postcardType;
    private String elements;
    private String status;
    private String reviewReason;
    private LocalDateTime scheduledAt;
    private Integer senderDeleted;
    private Integer recipientDeleted;
    private LocalDateTime createdAt;
}
