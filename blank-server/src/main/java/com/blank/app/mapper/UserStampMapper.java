package com.blank.app.mapper;

import com.blank.app.entity.UserStamp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserStampMapper {
    List<UserStamp> selectByUserAndStamp(@Param("userId") Integer userId, @Param("stampId") Integer stampId);
    List<UserStamp> selectByUserId(Integer userId);
    int insert(UserStamp userStamp);
    int updateById(UserStamp userStamp);
    int deleteById(Integer id);
    int deleteByUserId(Integer userId);
}
