package com.blank.app.service;

import org.springframework.scheduling.annotation.Async;

public interface ModerationService {
    @Async("asyncExecutor")
    void moderatePostcard(Integer postcardId);
}
