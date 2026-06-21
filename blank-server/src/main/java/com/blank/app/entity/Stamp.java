package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Stamp {
    private Integer id;
    private String seriesId;
    private String title;
    private String description;
    private Integer price;
    private String imagePath;
    private LocalDateTime createdAt;
}
