package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Checkin {
    private Integer id;
    private Integer userId;
    private LocalDate checkinDate;
    private Integer coinsEarned;
    private LocalDateTime createdAt;
}
