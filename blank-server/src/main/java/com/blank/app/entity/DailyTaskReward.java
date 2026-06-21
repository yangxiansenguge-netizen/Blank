package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyTaskReward {
    private Integer id;
    private Integer userId;
    private String taskType;
    private LocalDate rewardDate;
    private Integer coinsEarned;
    private LocalDateTime createdAt;
}
