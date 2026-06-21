package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StampSeries {
    private Integer id;
    private String name;
    private String folderName;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer stampCount;
}
