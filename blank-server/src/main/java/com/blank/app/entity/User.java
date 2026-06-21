package com.blank.app.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String uid;
    private String username;
    private String email;
    private String identity;
    private String passwordHash;
    private String avatar;
    private String vipLevel;
    private String vipPlanKey;
    private LocalDateTime vipExpiresAt;
    private Integer coins;
    private String gender;
    private LocalDate birthday;
    private String location;
    private String profileVisibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
