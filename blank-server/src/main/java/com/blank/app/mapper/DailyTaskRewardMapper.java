package com.blank.app.mapper;

import com.blank.app.entity.DailyTaskReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface DailyTaskRewardMapper {
    DailyTaskReward selectByUserTaskDate(@Param("userId") Integer userId,
                                          @Param("taskType") String taskType,
                                          @Param("rewardDate") LocalDate rewardDate);
    int insert(DailyTaskReward reward);
}
