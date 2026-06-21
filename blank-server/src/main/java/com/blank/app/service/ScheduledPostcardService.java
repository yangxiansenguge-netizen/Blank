package com.blank.app.service;

import com.blank.app.entity.Postcard;
import com.blank.app.mapper.PostcardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledPostcardService {
    private static final Logger log = LoggerFactory.getLogger(ScheduledPostcardService.class);

    @Autowired private PostcardMapper postcardMapper;
    @Autowired(required = false) private ModerationService moderationService;

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void processScheduledPostcards() {
        try {
            List<Postcard> scheduled = postcardMapper.selectScheduledToProcess();
            for (Postcard p : scheduled) {
                p.setStatus("reviewing");
                p.setReviewReason(null);
                if (postcardMapper.updateById(p) > 0 && moderationService != null) {
                    moderationService.moderatePostcard(p.getId());
                }
            }
        } catch (Exception e) {
            log.error("处理定时明信片失败", e);
        }
    }
}
