package com.blank.app.mapper;

import com.blank.app.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CheckinMapper {
    Checkin selectByUserAndDate(@Param("userId") Integer userId, @Param("date") LocalDate date);
    List<Checkin> selectByUserAndMonth(@Param("userId") Integer userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    int insert(Checkin checkin);
    int deleteByUserId(Integer userId);
}
